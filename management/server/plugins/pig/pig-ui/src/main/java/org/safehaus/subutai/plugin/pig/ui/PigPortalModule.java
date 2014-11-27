package org.safehaus.subutai.plugin.pig.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class PigPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "pig.png";
    protected static final Logger LOG = Logger.getLogger( PigPortalModule.class.getName() );
    private ExecutorService executor;
    private final Hadoop hadoop;
    private final Pig pig;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;


    public PigPortalModule( Pig pig, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager)
    {
        this.pig = pig;
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
        return PigConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return PigConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( PigPortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new PigComponent( executor, pig, hadoop, tracker, environmentManager );
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
