/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mahout;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.mahout.Config;
import org.safehaus.kiskis.mgmt.api.mahout.Mahout;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class MahoutImpl implements Mahout {

    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Installing cluster over %s", config.getClusterName()));
        if (po == null) {
            return null;
        }

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                //check if node agent is connected
                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                //check installed ksks packages
                Task checkInstalled = taskRunner.executeTask(Tasks.getCheckInstalledTask(config.getNodes()));

                if (!checkInstalled.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();

                    Result result = checkInstalled.getResults().get(node.getUuid());

                    if (result.getStdOut().contains("ksks-mahout")) {
                        po.addLog(String.format("Node %s already has Mahout installed. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Mahout");
                    //install mahout            

                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(config.getNodes()));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone("Installation succeeded\nDone");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s",
                                installTask.getResults().entrySet().iterator().next().getValue().getStdErr()));
                    }
                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }
            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(String clusterName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID checkNode(String clusterName, String lxcHostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID addNode(String clusterName, String lxcHostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID destroyNode(String clusterName, String lxcHostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Config> getClusters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
