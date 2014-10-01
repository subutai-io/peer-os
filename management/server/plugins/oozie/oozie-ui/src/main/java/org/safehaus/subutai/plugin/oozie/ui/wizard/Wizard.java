/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui.wizard;


import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;

public class Wizard
{

    private final VerticalLayout vlayout;
    private int step = 1;
    private OozieClusterConfig config = new OozieClusterConfig();
    private final ExecutorService executor;
    private Hadoop hadoopManager;
    private final Oozie oozieManager;

    private final Tracker tracker;

    public Wizard( final ExecutorService executorService, final ServiceLocator serviceLocator ) throws NamingException {

        this.tracker = serviceLocator.getService( Tracker.class );
        hadoopManager = serviceLocator.getService( Hadoop.class );
        oozieManager = serviceLocator.getService( Oozie.class );
        this.executor = executorService;
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        vlayout.setMargin( true );
        putForm();
    }


    private void putForm()
    {
        vlayout.removeAllComponents();
        switch ( step )
        {
            case 1:
            {
                vlayout.addComponent( new StepStart( this ) );
                break;
            }
            case 2:
            {
                vlayout.addComponent( new ConfigurationStep( this ) );
                break;
            }
            case 3:
            {
                vlayout.addComponent( new StepSetConfig( this ) );
                break;
            }
            case 4:
            {
                vlayout.addComponent( new VerificationStep( this ) );
                break;
            }
            default:
            {
                step = 1;
                vlayout.addComponent( new StepStart( this ) );
                break;
            }
        }
    }


    public Component getContent()
    {
        return vlayout;
    }


    public void next()
    {
        step++;
        putForm();
    }


    public void back()
    {
        step--;
        putForm();
    }


    public void cancel()
    {
        step = 1;
        putForm();
    }


    public void init()
    {
        step = 1;
        config = new OozieClusterConfig();
        putForm();
    }


    public OozieClusterConfig getConfig()
    {
        return config;
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public Oozie getOozieManager() {
        return oozieManager;
    }
}
