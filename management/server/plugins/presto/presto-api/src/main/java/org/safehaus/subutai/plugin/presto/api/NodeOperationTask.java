package org.safehaus.subutai.plugin.presto.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.impl.AbstractNodeOperationTask;


/**
 * Created by ebru on 13.11.2014.
 */
public class NodeOperationTask extends AbstractNodeOperationTask implements Runnable
{
    private final String clusterName;
    private final ContainerHost containerHost;
    private final Presto presto;
    private final NodeOperationType operationType;

    public NodeOperationTask( Presto presto, Tracker tracker, String clusterName, ContainerHost containerHost,
                              NodeOperationType operationType, CompleteEvent completeEvent, UUID trackID )
    {
        super( tracker, presto.getCluster( clusterName ), completeEvent, trackID, containerHost );
        this.presto = presto;
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
                trackID = presto.startNode( clusterName, containerHost.getHostname() );
                break;
            case STOP:
                trackID = presto.stopNode( clusterName, containerHost.getHostname() );
                break;
            case STATUS:
                trackID = presto.checkNode( clusterName, containerHost.getHostname() );
                break;

        }
        return trackID;
    }


    @Override
    public String getProductStoppedIdentifier()
    {
        return "Not running";
    }


    @Override
    public String getProductRunningIdentifier()
    {
        return "Running as";
    }
}
