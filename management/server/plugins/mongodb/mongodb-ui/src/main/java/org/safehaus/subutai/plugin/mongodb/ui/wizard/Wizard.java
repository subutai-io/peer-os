/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


/**
 * @author dilshat
 */
public class Wizard
{

    private final GridLayout grid;
    private final Mongo mongo;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private int step = 1;
    private MongoClusterConfig mongoClusterConfig;// = new MongoClusterConfigImpl();


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executorService = executorService;
        this.mongo = serviceLocator.getService( Mongo.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.mongoClusterConfig = mongo.newMongoClusterConfigInstance();
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
                component = new ConfigurationStep( this );
                break;
            }
            case 3:
            {
                component = new VerificationStep( mongo, executorService, tracker, this );
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
        mongoClusterConfig = mongo.newMongoClusterConfigInstance();// new MongoClusterConfigImpl();
        putForm();
    }


    public MongoClusterConfig getMongoClusterConfig()
    {
        return mongoClusterConfig;
    }
}
