package org.safehaus.subutai.plugin.flume.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final Hadoop hadoop;
    private final Flume flume;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private FlumeConfig config = new FlumeConfig();
    private HadoopClusterConfig hadoopConfig;


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.executorService = executorService;
        this.flume = serviceLocator.getService( Flume.class );
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
                component = new ConfigurationStep( hadoop, this, environmentManager  );
                break;
            }
            case 3:
            {
                component = new VerificationStep( flume, executorService, tracker, environmentManager, this );
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
        config = new FlumeConfig();
        hadoopConfig = new HadoopClusterConfig();
        putForm();
    }


    public FlumeConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }

    public void setHadoopConfig(HadoopClusterConfig hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
    }
}
