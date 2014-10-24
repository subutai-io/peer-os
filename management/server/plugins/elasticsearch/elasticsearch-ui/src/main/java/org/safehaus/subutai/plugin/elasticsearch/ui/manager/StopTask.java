package org.safehaus.subutai.plugin.elasticsearch.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class StopTask implements Runnable
{

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;
    private final Elasticsearch elasticsearch;
    private final Tracker tracker;


    public StopTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName, String lxcHostname,
                     CompleteEvent completeEvent )
    {
        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        UUID trackID = elasticsearch.stopNode( clusterName, hostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po =
                    tracker.getTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
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
