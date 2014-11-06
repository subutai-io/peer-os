package org.safehaus.subutai.plugin.storm.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;


public class StormNodeOperationTask extends AbstractNodeOperationTask implements Runnable
{
    private final String clusterName;
    private final ContainerHost containerHost;
    private final CompleteEvent completeEvent;
    private final Storm storm;
    private final Tracker tracker;
    private OperationType operationType;


    public StormNodeOperationTask( Storm storm, Tracker tracker, String clusterName,
                                   ContainerHost containerHost, OperationType operationType,
                                   CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, operationType, storm.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.storm = storm;
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
                trackID = storm.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = storm.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = storm.checkNode( clusterName, containerHost.getHostname() );
                break;
        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "is not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "is running";
    }
}
