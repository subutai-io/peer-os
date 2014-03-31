/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mahout;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.mahout.Config;
import org.safehaus.kiskis.mgmt.api.mahout.Mahout;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

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

    public UUID installCluster(Config config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID uninstallCluster(String clusterName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID startNode(String clusterName, String lxcHostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID stopNode(String clusterName, String lxcHostname) {
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
