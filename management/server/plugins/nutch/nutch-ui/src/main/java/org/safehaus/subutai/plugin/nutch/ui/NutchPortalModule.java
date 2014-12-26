/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.nutch.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.nutch.api.Nutch;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class NutchPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "nutch.png";
    protected static final Logger LOG = Logger.getLogger( NutchPortalModule.class.getName() );
    private ExecutorService executor;
    private final Nutch nutch;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final EnvironmentManager environmentManager;


    public NutchPortalModule( Nutch nutch, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager )
    {
        this.nutch = nutch;
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
        return NutchConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return NutchConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( NutchPortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new NutchComponent( executor, nutch, hadoop, tracker, environmentManager );
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
