package org.safehaus.subutai.plugin.elasticsearch.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;


public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable
{
    private final String clusterName;
    private final ContainerHost containerHost;
    private final Elasticsearch elasticsearch;
    private NodeOperationType operationType;


    public NodeOperationTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName,
                              ContainerHost containerHost, NodeOperationType operationType, CompleteEvent completeEvent,
                              UUID trackID )
    {
        super( tracker, elasticsearch.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.elasticsearch = elasticsearch;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
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
