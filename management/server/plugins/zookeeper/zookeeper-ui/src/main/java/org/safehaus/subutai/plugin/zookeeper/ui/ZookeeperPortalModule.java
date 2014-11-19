package org.safehaus.subutai.plugin.zookeeper.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;


public class ZookeeperPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "zookeeper.png";
    protected static final Logger LOG = LoggerFactory.getLogger( ZookeeperPortalModule.class.getName() );
    private final ServiceLocator serviceLocator;
    private ExecutorService executor;


    public ZookeeperPortalModule()
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
        return ZookeeperClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return ZookeeperClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( ZookeeperPortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new ZookeeperComponent( executor, serviceLocator );
        }
        catch ( NamingException e )
        {
            LOG.error( e.getMessage() );
        }
        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}
