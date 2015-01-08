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
    private final Cassandra cassandra;
    private NodeOperationType operationType;


    public NodeOperationTask( Cassandra cassandra, Tracker tracker, String clusterName,
                              ContainerHost containerHost, NodeOperationType operationType, CompleteEvent completeEvent,
                              UUID trackID )
    {
        super( tracker, cassandra.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.cassandra = cassandra;
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
                trackID = cassandra.startService( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = cassandra.stopService( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = cassandra.statusService( clusterName, containerHost.getHostname() );
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
