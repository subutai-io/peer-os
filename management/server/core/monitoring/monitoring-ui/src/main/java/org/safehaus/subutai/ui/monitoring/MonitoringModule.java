package org.safehaus.subutai.ui.monitoring;


import com.vaadin.ui.Component;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.monitoring.Monitor;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;


public class MonitoringModule implements PortalModule {

    private static final String MODULE_NAME = "Monitoring";

    public static final String MODULE_IMAGE = "monitoring.png";

    private Monitor monitor;
    private AgentManager agentManager;


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
        return FileUtil.getFile( MonitoringModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new ModuleView( monitor, agentManager );
    }


    public void setMonitor( Monitor monitor ) {
        this.monitor = monitor;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }
}
