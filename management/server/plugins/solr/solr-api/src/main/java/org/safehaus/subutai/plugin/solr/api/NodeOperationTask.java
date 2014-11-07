/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;


public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable
{

    private final String clusterName;
    private final ContainerHost containerHost;
    private final CompleteEvent completeEvent;
    private final Solr solr;
    private final Tracker tracker;
    private final NodeOperationType operationType;


    public NodeOperationTask( Solr solr, Tracker tracker, String clusterName, ContainerHost containerHost,
                              NodeOperationType operationType, CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, solr.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.solr = solr;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
        this.operationType = operationType;
        this.completeEvent = completeEvent;
    }

    @Override
    public UUID runTask()
    {

        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = solr.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = solr.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = solr.checkNode( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "Solr is not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "Solr is running";
    }
}
