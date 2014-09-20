package org.safehaus.subutai.plugin.lucene.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.lucene.api.Config;
import org.safehaus.subutai.plugin.lucene.api.Lucene;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final Hadoop hadoop;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final Lucene lucene;
    private int step = 1;
    private Config config = new Config();
    private HadoopClusterConfig hadoopConfig = new HadoopClusterConfig();


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.lucene = serviceLocator.getService( Lucene.class );

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
                component = new VerificationStep( lucene, executorService, tracker, this );
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


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
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
        config = new Config();
        putForm();
    }


    public Config getConfig()
    {
        return config;
    }
}
