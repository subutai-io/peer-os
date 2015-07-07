package io.subutai.core.metric.ui;


import java.io.File;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;


public class MonitorPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "monitor.png";
    public static final String MODULE_NAME = "Monitor";
    private static final Logger LOG = LoggerFactory.getLogger( MonitorPortalModule.class.getName() );
    protected ServiceLocator serviceLocator;
    private HostRegistry hostRegistry;


    public MonitorPortalModule(final HostRegistry hostRegistry)
    {
        this.serviceLocator = new ServiceLocator();
        this.hostRegistry = hostRegistry;
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
        return FileUtil.getFile( MonitorPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new MonitorForm( serviceLocator, hostRegistry );
        }
        catch ( NamingException e )
        {
            LOG.error( "Error in createComponent", e );
        }

        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
