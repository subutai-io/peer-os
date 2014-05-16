package org.safehaus.kiskis.mgmt.ui.lxcmanager;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import com.vaadin.ui.Component;


public class LxcUI implements Module {

    public static final String MODULE_NAME = "LXC";
    private static ExecutorService executor;
    private AgentManager agentManager;
    private LxcManager lxcManager;


    public static ExecutorService getExecutor() {
        return executor;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setLxcManager( LxcManager lxcManager ) {
        this.lxcManager = lxcManager;
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
        return new LxcForm( agentManager, lxcManager );
    }
}
