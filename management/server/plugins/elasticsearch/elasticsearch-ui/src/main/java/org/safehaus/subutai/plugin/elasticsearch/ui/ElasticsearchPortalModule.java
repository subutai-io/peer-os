package org.safehaus.subutai.plugin.elasticsearch.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ElasticsearchPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "logo.jpeg";
    private static Logger LOG = Logger.getLogger( ElasticsearchPortalModule.class.getName() );

    private ExecutorService executor;
    private Elasticsearch elasticsearch;
    private Tracker tracker;
    private EnvironmentManager environmentManager;


    public ElasticsearchPortalModule(Elasticsearch elasticsearch, Tracker tracker, EnvironmentManager environmentManager)
    {

        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
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
        return FileUtil.getFile( ElasticsearchPortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new ElasticsearchComponent( executor, elasticsearch, tracker, environmentManager );
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
