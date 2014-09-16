package org.safehaus.subutai.plugin.elasticsearch.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Config;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;


public class Wizard {

    private static final int MAX_STEPS = 3;
    private final VerticalLayout verticalLayout;
    GridLayout grid;
    private int step = 1;
    private Config config = new Config();

    private final ExecutorService executorService;
    private final Tracker tracker;
    private final Elasticsearch elasticsearch;


    public Wizard( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException {

        this.elasticsearch = serviceLocator.getService( Elasticsearch.class );
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );

        verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        grid = new GridLayout( 1, 1 );
        grid.setMargin( true );
        grid.setSizeFull();
        grid.addComponent( verticalLayout );
        grid.setComponentAlignment( verticalLayout, Alignment.TOP_CENTER );

        putForm();
    }


    private void putForm() {
        verticalLayout.removeAllComponents();
        switch ( step ) {
            case 1: {
                verticalLayout.addComponent( new StepStart( this ) );
                break;
            }
            case 2: {
                verticalLayout.addComponent( new ConfigurationStep( this ) );
                break;
            }
            case 3: {
                verticalLayout.addComponent( new VerificationStep( elasticsearch, executorService, tracker, this ) );
                break;
            }
            default: {
                step = 1;
                verticalLayout.addComponent( new StepStart( this ) );
                break;
            }
        }
    }


    public Component getContent() {
        return grid;
    }


    protected void next() {
        step++;
        putForm();
    }


    protected void back() {
        step--;
        putForm();
    }


    protected void cancel() {
        step = 1;
        putForm();
    }


    public Config getConfig() {
        return config;
    }


    public void init() {
        step = 1;
        config = new Config();
        putForm();
    }
}
