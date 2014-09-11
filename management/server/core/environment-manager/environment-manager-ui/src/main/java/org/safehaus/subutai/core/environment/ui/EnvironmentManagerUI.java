package org.safehaus.subutai.core.environment.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class EnvironmentManagerUI implements PortalModule {

    public static final String MODULE_IMAGE = "env.png";
    public static final String MODULE_NAME = "Environment";
    private static ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;


    public static ExecutorService getExecutor() {
        return executor;
    }


    public static void setExecutor( final ExecutorService executor ) {
        EnvironmentManagerUI.executor = executor;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }

    //    private AgentManager agentManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    //    public void setAgentManager( AgentManager agentManager ) {
    //        this.agentManager = agentManager;
    //    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    @Override
    public String getId() {
        return MODULE_NAME;
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( EnvironmentManagerUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new EnvironmentManagerForm( this );
    }
}
