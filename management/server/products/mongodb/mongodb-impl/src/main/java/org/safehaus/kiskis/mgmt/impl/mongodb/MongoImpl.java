/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.server.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.server.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

/**
 *
 * @author dilshat
 */
public class MongoImpl implements Mongo {

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

    public List<Config> getClusters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public boolean startNode(Config config, Agent node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean stopNode(Config config, Agent node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public NodeState checkNode(Config config, Agent node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
