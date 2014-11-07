package org.safehaus.subutai.plugin.spark.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final ExecutorService executor;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final Spark spark;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private SparkClusterConfig config = new SparkClusterConfig();
    private HadoopClusterConfig hadoopConfig = new HadoopClusterConfig();


    public Wizard( ExecutorService executor, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executor = executor;

        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.spark = serviceLocator.getService( Spark.class );
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
                component = new ConfigurationStep( hadoop, environmentManager, this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( tracker, spark, executor, environmentManager, this );
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
        config = new SparkClusterConfig();
        hadoopConfig = new HadoopClusterConfig();
        putForm();
    }


    public SparkClusterConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }
}
