/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final Hadoop hadoop;
    private final Accumulo accumulo;
    private final Zookeeper zookeeper;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private AccumuloClusterConfig config = new AccumuloClusterConfig();
    private HadoopClusterConfig hadoopClusterConfig = new HadoopClusterConfig();
    private ZookeeperClusterConfig zookeeperClusterConfig = new ZookeeperClusterConfig();


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executorService = executorService;
        this.accumulo = serviceLocator.getService( Accumulo.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.zookeeper = serviceLocator.getService( Zookeeper.class );
        this.tracker = serviceLocator.getService( Tracker.class );
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
                component = new ConfigurationStep( hadoop, zookeeper, environmentManager, this );
                break;
            }
            case 3:
            {
                component =
                        new VerificationStep( accumulo, hadoop, executorService, tracker, environmentManager, this );
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
        config = new AccumuloClusterConfig();
        hadoopClusterConfig = new HadoopClusterConfig();
        zookeeperClusterConfig = new ZookeeperClusterConfig();
        putForm();
    }


    public AccumuloClusterConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopClusterConfig()
    {
        return hadoopClusterConfig;
    }


    public ZookeeperClusterConfig getZookeeperClusterConfig()
    {
        return zookeeperClusterConfig;
    }
}
