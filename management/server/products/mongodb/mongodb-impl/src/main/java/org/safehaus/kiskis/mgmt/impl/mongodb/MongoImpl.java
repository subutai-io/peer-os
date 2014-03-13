/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.api.mongodb.Mongo;

/**
 *
 * @author dilshat
 */
public class MongoImpl implements Mongo {

    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;

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

    @Override
    public String getClusters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String installCluster(String config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String uninstallCluster(String config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String startNode(String config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String stopNode(String config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String checkNode(String config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
