package org.safehaus.subutai.plugin.zookeeper.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final Zookeeper zookeeper;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private ZookeeperClusterConfig config = new ZookeeperClusterConfig();
    private HadoopClusterConfig hadoopClusterConfig = new HadoopClusterConfig();


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.zookeeper = serviceLocator.getService( Zookeeper.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
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
                component = new ConfigurationStep( hadoop, this, environmentManager );
                break;
            }
            case 3:
            {
                component = new VerificationStep( zookeeper, executorService, tracker, this, environmentManager );
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
        config = new ZookeeperClusterConfig();
        hadoopClusterConfig = new HadoopClusterConfig();
        putForm();
    }


    public ZookeeperClusterConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopClusterConfig()
    {
        return hadoopClusterConfig;
    }


    public void setHadoopClusterConfig( final HadoopClusterConfig hadoopClusterConfig )
    {
        this.hadoopClusterConfig = hadoopClusterConfig;
    }
}
