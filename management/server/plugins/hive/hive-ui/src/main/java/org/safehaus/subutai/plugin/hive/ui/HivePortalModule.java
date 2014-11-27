package org.safehaus.subutai.plugin.hive.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class HivePortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "hive.png";
    protected static final Logger LOG = Logger.getLogger( HivePortalModule.class.getName() );
    private ExecutorService executor;
    private final Hive hive;
    private final EnvironmentManager environmentManager;
    private final Tracker tracker;
    private Hadoop hadoop;


    public HivePortalModule( Hive hive, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager)
    {
        this.hive = hive;
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
        return HiveConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return HiveConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( HivePortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new HiveComponent( executor, hive, hadoop, tracker, environmentManager );
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
