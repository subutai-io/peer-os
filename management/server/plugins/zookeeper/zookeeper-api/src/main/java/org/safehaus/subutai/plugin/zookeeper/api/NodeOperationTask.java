package org.safehaus.subutai.plugin.zookeeper.api;


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
    private final Zookeeper zookeeper;
    private NodeOperationType operationType;


    public NodeOperationTask( Zookeeper zookeeper, Tracker tracker, String clusterName, ContainerHost containerHost,
                              NodeOperationType operationType, CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, zookeeper.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.zookeeper = zookeeper;
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
                trackID = zookeeper.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = zookeeper.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = zookeeper.checkNode( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "Zookeeper is NOT Running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "Zookeeper is Running";
    }
}
