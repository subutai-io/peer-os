package org.safehaus.subutai.plugin.elasticsearch.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;


public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable
{
    private final String clusterName;
    private final ContainerHost containerHost;
    private final CompleteEvent completeEvent;
    private final Elasticsearch elasticsearch;
    private final Tracker tracker;
    private OperationType operationType;


    public NodeOperationTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName,
                              ContainerHost containerHost, OperationType operationType, CompleteEvent completeEvent,
                              UUID trackID )
    {
        super( tracker, operationType, elasticsearch.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
        this.completeEvent = completeEvent;
        this.operationType = operationType;
    }


    @Override
    public UUID runTask()
    {
        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = elasticsearch.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = elasticsearch.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = elasticsearch.checkNode( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "elasticsearch is not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "elasticsearch is running";
    }
}
