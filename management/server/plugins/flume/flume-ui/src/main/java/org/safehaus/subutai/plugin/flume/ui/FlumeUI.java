package org.safehaus.subutai.plugin.flume.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class FlumeUI implements PortalModule
{
    public static final String MODULE_IMAGE = "flume.png";
    protected static final Logger LOG = Logger.getLogger( FlumeUI.class.getName() );
    private final ServiceLocator serviceLocator;
    private ExecutorService executor;


    public FlumeUI()
    {
        this.serviceLocator = new ServiceLocator();
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
        return FlumeConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return FlumeConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( FlumeUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new FlumeForm( executor, serviceLocator );
        }
        catch ( NamingException e )
        {
            LOG.severe( e.getMessage() );
        }

        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}
