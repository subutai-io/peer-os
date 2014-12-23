/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * @author dilshat
 */
public class MongoPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "mongodb.png";
    protected final Logger LOG = Logger.getLogger( MongoPortalModule.class.getName() );
    private ExecutorService executor;
    private final Tracker tracker;
    private final Mongo mongo;
    private final EnvironmentManager environmentManager;


    public MongoPortalModule( Mongo mongo,EnvironmentManager environmentManager, Tracker tracker )
    {

        this.mongo = mongo;
        this.environmentManager = environmentManager;
        this.tracker = tracker;
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
        return MongoClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return MongoClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MongoPortalModule.MODULE_IMAGE, this );
    }

    public Component createComponent()
    {
        try
        {
            return new MongoComponent( executor, mongo, environmentManager, tracker );
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
