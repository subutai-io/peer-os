package org.safehaus.subutai.plugin.sqoop.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class SqoopPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "sqoop.png";
    protected static final Logger LOG = Logger.getLogger( SqoopPortalModule.class.getName() );
    private ExecutorService executor;
    private final Sqoop sqoop;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;;
    private Hadoop hadoop;


    public SqoopPortalModule( Sqoop sqoop, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager )
    {
        this.sqoop = sqoop;
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
        return SqoopConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return SqoopConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( SqoopPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new SqoopComponent( executor, sqoop, hadoop, tracker, environmentManager );
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
