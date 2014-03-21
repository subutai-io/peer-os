/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.server.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.server.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

/**
 *
 * @author dilshat
 */
public class MongoImpl implements Mongo {

    private static final Logger LOG = Logger.getLogger(MongoImpl.class.getName());

    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static LxcManager lxcManager;
    private static ExecutorService executor;

    public static void setLxcManager(LxcManager lxcManager) {
        MongoImpl.lxcManager = lxcManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        MongoImpl.agentManager = agentManager;
    }

    public void setDbManager(DbManager dbManager) {
        MongoImpl.dbManager = dbManager;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        MongoImpl.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static LxcManager getLxcManager() {
        return lxcManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        MongoImpl.taskRunner = null;
        MongoImpl.agentManager = null;
        MongoImpl.dbManager = null;
        MongoImpl.lxcManager = null;
        executor.shutdown();
    }

    public UUID installCluster(Config config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID uninstallCluster(Config config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID addNode(Config config, NodeType nodeType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID destroyNode(Config config, Agent agent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Config> getClusters() {
        try {

            return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getClusters", ex);
        }

        return new ArrayList<Config>();
    }

    public boolean startNode(Config config, Agent node) {
        Task startNodeTask;
        NodeType nodeType = getNodeType(config, node);

        if (nodeType == NodeType.CONFIG_NODE) {
            startNodeTask = Tasks.getStartConfigServersTask(
                    Util.wrapAgentToSet(node), config);

        } else if (nodeType == NodeType.DATA_NODE) {
            startNodeTask = Tasks.getStartReplicaSetTask(
                    Util.wrapAgentToSet(node), config);
        } else {
            startNodeTask = Tasks.getStartRoutersTask(
                    Util.wrapAgentToSet(node),
                    config.getConfigServers(),
                    config);
        }

        taskRunner.executeTask(startNodeTask, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (stdOut.indexOf("child process started successfully, parent exiting") > -1) {

                    taskRunner.removeTaskCallback(task.getUuid());
                    task.setData(NodeState.RUNNING);
                    synchronized (task) {
                        task.notifyAll();
                    }
                } else if (task.isCompleted()) {
                    synchronized (task) {
                        task.notifyAll();
                    }
                }
                return null;
            }
        });

        synchronized (startNodeTask) {
            try {
                startNodeTask.wait(startNodeTask.getAvgTimeout() * 1000 + 1000);
            } catch (InterruptedException ex) {
            }
        }

        return NodeState.RUNNING.equals(startNodeTask.getData());
    }

    public boolean stopNode(Config config, Agent node) {
        Task stopNodeTask = taskRunner.executeTask(
                Tasks.getStopMongoTask(Util.wrapAgentToSet(node)));

        if (stopNodeTask.isCompleted()) {
            return checkNode(config, node) == NodeState.STOPPED;
        }

        return false;
    }

    public NodeState checkNode(Config config, Agent node) {
        Task checkNodeTask = taskRunner.executeTask(
                Tasks.getCheckStatusTask(Util.wrapAgentToSet(node), getNodeType(config, node), config));

        if (checkNodeTask.isCompleted()) {
            String stdOut = checkNodeTask.getResults().entrySet().iterator().next().getValue().getStdOut();
            if (stdOut.indexOf("couldn't connect to server") > -1) {
                return NodeState.STOPPED;
            } else if (stdOut.indexOf("connecting to") > -1) {
                return NodeState.RUNNING;
            }
        }

        return NodeState.UNKNOWN;
    }

    private NodeType getNodeType(Config config, Agent node) {
        NodeType nodeType = NodeType.DATA_NODE;

        if (config.getRouterServers().contains(node)) {
            nodeType = NodeType.ROUTER_NODE;
        } else if (config.getConfigServers().contains(node)) {
            nodeType = NodeType.CONFIG_NODE;
        }

        return nodeType;
    }

}
