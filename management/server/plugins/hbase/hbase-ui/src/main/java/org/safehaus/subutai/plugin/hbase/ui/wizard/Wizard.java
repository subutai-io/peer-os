/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final ServiceLocator serviceLocator;
    private final ExecutorService executor;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final HBase hbase;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private HBaseConfig config = new HBaseConfig();
    private HadoopClusterConfig hadoopConfig = new HadoopClusterConfig();


    public Wizard( ExecutorService executor, ServiceLocator serviceLocator ) throws NamingException
    {
        this.serviceLocator = serviceLocator;
        this.executor = executor;

        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.hbase = serviceLocator.getService( HBase.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        grid = new GridLayout( 1, 20 );
        grid.setMargin( true );
        grid.setSizeFull();
        putForm();
    }


    private void putForm()
    {
        grid.removeComponent( 0, 1 );
        Component component = null;
        switch ( step )
        {
            case 1:
            {
                component = new WelcomeStep( this );
                break;
            }
            case 2:
            {
                component = new ConfigurationStep( hadoop, this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( tracker, hbase, executor, this );
                break;
            }
            default:
            {
                break;
            }
        }

        if ( component != null )
        {
            grid.addComponent( component, 0, 1, 0, 19 );
        }
    }


    public Component getContent()
    {
        return grid;
    }


    protected void next()
    {
        step++;
        putForm();
    }


    protected void back()
    {
        step--;
        putForm();
    }


    protected void init()
    {
        step = 1;
        config = new HBaseConfig();
        hadoopConfig = new HadoopClusterConfig();
        putForm();
    }


    public HBaseConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }
}
