/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sound.midi.Track;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class AccumuloPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "accumulo.png";
    protected static final Logger LOG = Logger.getLogger( AccumuloPortalModule.class.getName() );
    private ExecutorService executor;
    private Accumulo accumulo;
    private Hadoop hadoop;
    private Zookeeper zookeeper;
    private Tracker tracker;
    private EnvironmentManager environmentManager;


    public AccumuloPortalModule(Accumulo accumulo, Tracker tracker, Hadoop hadoop, Zookeeper zookeeper, EnvironmentManager environmentManager)
    {
        this.accumulo = accumulo;
        this.tracker = tracker;
        this.hadoop = hadoop;
        this.zookeeper = zookeeper;
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
        return AccumuloClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return AccumuloClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( AccumuloPortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new AccumuloComponent( executor, accumulo, hadoop, zookeeper, tracker, environmentManager );
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
