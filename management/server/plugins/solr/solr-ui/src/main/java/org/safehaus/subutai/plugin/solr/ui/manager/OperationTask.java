/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;


public class OperationTask implements Runnable
{

    private final String clusterName;
    private final ContainerHost containerHost;
    private final CompleteEvent completeEvent;
    private final Solr solr;
    private final Tracker tracker;
    private final OperationType operationType;


    public OperationTask( Solr solr, Tracker tracker, String clusterName, ContainerHost containerHost,
                          OperationType operationType, CompleteEvent completeEvent )
    {
        this.solr = solr;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
        this.operationType = operationType;
        this.completeEvent = completeEvent;
    }


    public void run()
    {

        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = solr.startNode( clusterName, containerHost );
                break;
            case STOP:
                trackID = solr.stopNode( clusterName, containerHost );
                break;
            case STATUS:
                trackID = solr.checkNode( clusterName, containerHost );
                break;
        }

        long start = System.currentTimeMillis();

        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( SolrClusterConfig.PRODUCT_KEY, trackID );
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
