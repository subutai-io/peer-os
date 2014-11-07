package org.safehaus.subutai.plugin.storm.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final ExecutorService executorService;
    private final Storm storm;
    private final Tracker tracker;
    private final Zookeeper zookeeper;
    private int step = 1;
    private StormClusterConfiguration config = new StormClusterConfiguration();


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.storm = serviceLocator.getService( Storm.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.zookeeper = serviceLocator.getService( Zookeeper.class );

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
                component = new NodeSelectionStep( zookeeper, this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( storm, executorService, tracker, this );
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


    protected void init( boolean externalZookeeper )
    {
        step = 1;
        config = new StormClusterConfiguration();
        config.setExternalZookeeper( externalZookeeper );
        putForm();
    }


    public StormClusterConfiguration getConfig()
    {
        return config;
    }
}
