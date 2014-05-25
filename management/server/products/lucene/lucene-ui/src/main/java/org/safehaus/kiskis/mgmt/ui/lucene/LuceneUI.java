/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.lucene;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.api.lucene.Lucene;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class LuceneUI implements Module {

    private static Lucene luceneManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hadoop hadoopManager;
    private static ExecutorService executor;

    public LuceneUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Lucene luceneManager) {
        LuceneUI.agentManager = agentManager;
        LuceneUI.tracker = tracker;
        LuceneUI.hadoopManager = hadoopManager;
        LuceneUI.luceneManager = luceneManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Lucene getLuceneManager() {
        return luceneManager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        luceneManager = null;
        agentManager = null;
        hadoopManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new LuceneForm();
    }

}
