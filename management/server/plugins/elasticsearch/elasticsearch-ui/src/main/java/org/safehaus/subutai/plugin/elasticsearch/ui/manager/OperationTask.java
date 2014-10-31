package org.safehaus.subutai.plugin.elasticsearch.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class OperationTask implements Runnable
{
    private final String clusterName;
    private final UUID agentUUID;
    private final CompleteEvent completeEvent;
    private final Elasticsearch elasticsearch;
    private final Tracker tracker;
    private OperationType operationType;


    public OperationTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName, UUID agentUUID,
                          OperationType operationType, CompleteEvent completeEvent )
    {
        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.agentUUID = agentUUID;
        this.completeEvent = completeEvent;
        this.operationType = operationType;
    }


    @Override
    public void run()
    {
        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = elasticsearch.startNode( clusterName, agentUUID );
                break;
            case STOP:
                trackID = elasticsearch.stopNode( clusterName, agentUUID );
                break;
            case STATUS:
                trackID = elasticsearch.checkNode( clusterName, agentUUID );
                break;
        }

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
