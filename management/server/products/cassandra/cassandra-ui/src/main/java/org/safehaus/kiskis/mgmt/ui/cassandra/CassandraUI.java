/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.cassandra;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.cassandra.Cassandra;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class CassandraUI implements Module {

    private static Cassandra cassandraManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        CassandraUI.tracker = tracker;
    }

    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }

    public void setCassandraManager(Cassandra cassandraManager) {
        CassandraUI.cassandraManager = cassandraManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        CassandraUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        cassandraManager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new CassandraForm();
    }

}
