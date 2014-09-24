package org.safehaus.subutai.plugin.elasticsearch.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ElasticsearchUI implements PortalModule
{

    public static final String MODULE_IMAGE = "logo.jpeg";
    private final ServiceLocator serviceLocator;
    protected Logger LOG = Logger.getLogger( ElasticsearchUI.class.getName() );
    private ExecutorService executor;


    public ElasticsearchUI()
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
        return ElasticsearchClusterConfiguration.PRODUCT_KEY;
    }


    public String getName()
    {
        return ElasticsearchClusterConfiguration.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( ElasticsearchUI.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new ElasticsearchForm( executor, serviceLocator );
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
