package org.safehaus.subutai.plugin.cassandra.api;


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
    private final Cassandra elasticsearch;
    private NodeOperationType operationType;


    public NodeOperationTask( Cassandra elasticsearch, Tracker tracker, String clusterName,
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
                trackID = elasticsearch.startService( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = elasticsearch.stopService( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = elasticsearch.statusService( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "cassandra is not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "cassandra is running";
    }
}
