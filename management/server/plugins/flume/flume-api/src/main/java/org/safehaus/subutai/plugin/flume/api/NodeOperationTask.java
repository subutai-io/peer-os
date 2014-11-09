package org.safehaus.subutai.plugin.flume.api;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;

import java.util.UUID;

/**
 * Created by ebru on 09.11.2014.
 */
public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable {

    private final String clusterName;
    private final ContainerHost containerHost;
    private final CompleteEvent completeEvent;
    private final Flume flume;
    private final Tracker tracker;
    private final NodeOperationType operationType;

    public NodeOperationTask( Flume flume, Tracker tracker, String clusterName, ContainerHost containerHost,
                              NodeOperationType operationType, CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, flume.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.flume = flume;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.containerHost = containerHost;
        this.operationType = operationType;
        this.completeEvent = completeEvent;
    }

    @Override
    public UUID runTask() {
        UUID trackID = null;
        switch ( operationType )
        {
            case START:
                trackID = flume.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = flume.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = flume.checkNode( clusterName, containerHost.getHostname() );
                break;
            case SERVICE_STATUS:
                trackID = flume.checkServiceStatus( clusterName, containerHost.getHostname() );
                break;

        }
        return trackID;
    }

    @Override
    public String getProductStoppedIdentifier() {
        return null;
    }

    @Override
    public String getProductRunningIdentifier() {
        return null;
    }
}
