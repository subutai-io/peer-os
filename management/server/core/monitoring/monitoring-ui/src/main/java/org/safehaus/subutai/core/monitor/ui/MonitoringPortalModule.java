package org.safehaus.subutai.core.monitor.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class MonitoringPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "monitoring.png";
    private static final String MODULE_NAME = "Monitoring";
    private Monitoring monitoring;
    private AgentManager agentManager;


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
        return FileUtil.getFile( MonitoringPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new MonitoringComponent( monitoring, agentManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }


    public void setMonitoring( Monitoring monitoring )
    {
        this.monitoring = monitoring;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }
}
