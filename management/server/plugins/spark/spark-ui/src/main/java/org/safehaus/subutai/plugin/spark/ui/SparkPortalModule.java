package org.safehaus.subutai.plugin.spark.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class SparkPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "spark.png";
    protected static final Logger LOG = Logger.getLogger( SparkPortalModule.class.getName() );
    private ExecutorService executor;
    private final Spark spark;
    private final Tracker tracker;
    private final Hadoop hadoop;

    private final EnvironmentManager environmentManager;


    public SparkPortalModule( Spark spark, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager )
    {
        this.spark = spark;
        this.hadoop = hadoop;
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
        return SparkClusterConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return SparkClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( SparkPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new SparkComponent( executor, spark, hadoop, tracker, environmentManager );
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
