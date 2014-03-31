/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.solr;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.solr.Solr;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class SolrUI implements Module {

    public static final String MODULE_NAME = "Solr";
    private static Solr solrManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        SolrUI.tracker = tracker;
    }

    public static Solr getSolrManager() {
        return solrManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setSolrManager(Solr solrManager) {
        SolrUI.solrManager = solrManager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        SolrUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        solrManager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new SolrForm();
    }

}
