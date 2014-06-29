package org.safehaus.subutai.dis.manager;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.server.ui.services.Module;

import com.vaadin.ui.Component;


public class DisUI implements Module {

    public static final String MODULE_NAME = "LXC";
    private static ExecutorService executor;
    private AgentManager agentManager;


    public static ExecutorService getExecutor() {
        return executor;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public Component createComponent() {
        return new DisForm( agentManager );
    }
}
