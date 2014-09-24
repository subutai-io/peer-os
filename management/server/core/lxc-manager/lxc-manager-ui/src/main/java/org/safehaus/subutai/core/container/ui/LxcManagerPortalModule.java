package org.safehaus.subutai.core.container.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class LxcManagerPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "lxc.png";
    public static final String MODULE_NAME = "LXC";
    private ExecutorService executor;
    private AgentManager agentManager;
    private LxcManager lxcManager;


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setLxcManager( LxcManager lxcManager )
    {
        this.lxcManager = lxcManager;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( LxcManagerPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new LxcManagerComponent( agentManager, lxcManager, executor );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
