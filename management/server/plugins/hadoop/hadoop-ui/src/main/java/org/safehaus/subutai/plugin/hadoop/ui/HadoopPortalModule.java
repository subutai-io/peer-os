package org.safehaus.subutai.plugin.hadoop.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * Hadoop UI
 */
public class HadoopPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "hadoop.png";
    protected static final Logger LOG = Logger.getLogger( HadoopPortalModule.class.getName() );
    private ExecutorService executor;
    private Tracker tracker;
    private Hadoop hadoop;
    private EnvironmentManager environmentManager;
    private HostRegistry hostRegistry;


    public HadoopPortalModule(Tracker tracker, Hadoop hadoop, EnvironmentManager environmentManager, HostRegistry hostRegistry )
    {
        this.tracker = tracker;
        this.hadoop = hadoop;
        this.environmentManager = environmentManager;
        this.hostRegistry = hostRegistry;

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
        return HadoopClusterConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return HadoopClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( HadoopPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new HadoopComponent( executor, tracker, hadoop, environmentManager, hostRegistry );
        }
        catch ( NamingException e )
        {
            LOG.severe( String.format( "Error creating HadoopPortalModule bundle:\n%s", e.getMessage() ) );
        }

        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}
