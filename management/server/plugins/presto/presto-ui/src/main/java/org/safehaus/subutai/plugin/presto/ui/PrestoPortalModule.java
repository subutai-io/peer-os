package org.safehaus.subutai.plugin.presto.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class PrestoPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "presto.png";
    protected static final Logger LOG = Logger.getLogger( PrestoPortalModule.class.getName() );
    private ExecutorService executor;
    private final Presto presto;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;


    public PrestoPortalModule( Presto presto, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager)
    {
        this.presto = presto;
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
        return PrestoClusterConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return PrestoClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( PrestoPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {

        try
        {
            return new PrestoComponent( executor, presto, hadoop, tracker, environmentManager );
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
