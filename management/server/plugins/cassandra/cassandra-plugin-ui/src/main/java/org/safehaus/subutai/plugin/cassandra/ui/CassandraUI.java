/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class CassandraUI implements PortalModule
{

    public static final String MODULE_IMAGE = "cassandra.png";
    protected Logger LOG = Logger.getLogger( CassandraUI.class.getName() );

    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public CassandraUI()
    {
        this.serviceLocator = new ServiceLocator();
    }


    public CassandraUI( String ui )
    {
        this.serviceLocator = new ServiceLocator();
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
        return CassandraClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return CassandraClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( CassandraUI.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        try
        {
            return new CassandraForm( executor, serviceLocator );
        }
        catch ( NamingException e )
        {
            LOG.severe( e.getMessage() );
            ;
        }
        return null;
    }
}
