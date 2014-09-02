package org.safehaus.subutai.plugin.hadoop.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class HadoopUI implements PortalModule {

    public static final String MODULE_IMAGE = "hadoop.png";

    private static Hadoop hadoopManager;
    private static AgentManager agentManager;
    private static ExecutorService executor;
    private static Tracker tracker;


    public HadoopUI( AgentManager agentManager, Hadoop hadoopManager, Tracker tracker ) {
        HadoopUI.agentManager = agentManager;
        HadoopUI.hadoopManager = hadoopManager;
        HadoopUI.tracker = tracker;
    }


    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public static AgentManager getAgentManager() {
        return agentManager;
    }


    public static ExecutorService getExecutor() {
        return executor;
    }


    public static Tracker getTracker() {
        return tracker;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        tracker = null;
        hadoopManager = null;
        agentManager = null;
        executor.shutdown();
    }


    @Override
    public String getId() {
        return HadoopClusterConfig.PRODUCT_KEY;
    }


    @Override
    public String getName() {
        return HadoopClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( HadoopUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new HadoopForm();
    }
}
