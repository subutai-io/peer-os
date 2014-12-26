package org.safehaus.subutai.plugin.shark.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final ExecutorService executor;
    private final Tracker tracker;
    private final Spark spark;
    private final Shark shark;
    private int step = 1;
    private SharkClusterConfig config;
    private HadoopClusterConfig hadoopConfig;


    public Wizard( ExecutorService executorService, Shark shark, Spark spark, Tracker tracker ) throws NamingException
    {

        this.executor = executorService;
        this.shark = shark;
        this.spark = spark;
        this.tracker = tracker;

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
                component = new ConfigurationStep( spark, this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( shark, executor, tracker, this );
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
        config = new SharkClusterConfig();
        hadoopConfig = new HadoopClusterConfig();
        putForm();
    }


    public SharkClusterConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }
}

