/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class HBasePortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "hbase.png";
    private static final Logger LOG = Logger.getLogger( HBasePortalModule.class.getName() );
    private ExecutorService executor;
    private HBase hBase;
    private Hadoop hadoop;
    private Tracker tracker;
    private  EnvironmentManager environmentManager;


    public HBasePortalModule(HBase hBase, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager)
    {
        this.hBase = hBase;
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.environmentManager = environmentManager;

    }


    public void init()
    {
        LOG.info( "HBase module initializing..." );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return HBaseConfig.PRODUCT_KEY;
    }


    @Override
    public String getName()
    {
        return HBaseConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( HBasePortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new HBaseComponent( executor, hBase, hadoop, tracker, environmentManager );
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
