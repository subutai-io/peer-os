package org.safehaus.subutai.plugin.hive.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public abstract class Wizard
{

    private final GridLayout grid;
    private final Hive hive;
    private final Hadoop hadoop;
    private final ExecutorService executorService;
    private final EnvironmentManager environmentManager;
    private final Tracker tracker;
    private int step = 1;
    private HiveConfig config = new HiveConfig();
    private HadoopClusterConfig hadoopConfig;


    public Wizard( ExecutorService executorService, Hive hive, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {

        this.executorService = executorService;
        this.hive = hive;
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.environmentManager = environmentManager;

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
                component = new NodeSelectionStep( hive, hadoop, environmentManager, this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( hive, executorService, tracker, environmentManager, this );
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
        config = new HiveConfig();
        hadoopConfig = new HadoopClusterConfig();
        putForm();
    }


    public abstract void requestHadoopPlugin();


    public HiveConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }


    public void setHadoopConfig( final HadoopClusterConfig hadoopConfig )
    {
        this.hadoopConfig = hadoopConfig;
    }
}
