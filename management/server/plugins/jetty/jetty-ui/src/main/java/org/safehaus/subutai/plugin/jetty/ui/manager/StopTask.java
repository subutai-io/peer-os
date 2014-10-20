package org.safehaus.subutai.plugin.jetty.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.jetty.api.Jetty;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;


public class StopTask implements Runnable
{

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private Jetty jetty;
    private Tracker tracker;


    public StopTask( Jetty jetty, Tracker tracker, String clusterName, String lxcHostname,
                     CompleteEvent completeEvent )
    {
        this.jetty = jetty;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        UUID trackID = jetty.stopService( clusterName, lxcHostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            ProductOperationView po = tracker.getProductOperation( JettyConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    completeEvent.onComplete( po.getLog() );
                    break;
                }
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }
    }
}
