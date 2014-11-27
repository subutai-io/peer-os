package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerCreatorThread extends Observable implements Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerCreatorThread.class.getName() );
    private CloneContainersMessage message;
    private PeerManager peerManager;
    private UUID environmentId;


    public ContainerCreatorThread( final EnvironmentBuilderImpl environmentBuilder, final UUID environmentId,
                                   final CloneContainersMessage message, final PeerManager peerManager )
    {
        this.environmentId = environmentId;
        this.message = message;
        this.peerManager = peerManager;
    }


    @Override
    public void run()
    {
        try
        {
            Set<ContainerHost> containers = peerManager.getPeer( message.getTargetPeerId() ).
                    createContainers( message.getTargetPeerId(), environmentId, message.getTemplates(),
                            message.getNumberOfNodes(), message.getStrategy().getStrategyId(),
                            message.getStrategy().getCriteriaAsList(), message.getNodeGroupName() );
            LOG.info( String.format( "Received %d containers for environment %s", containers.size(), environmentId ) );
            setChanged();
            notifyObservers( containers );
        }
        catch ( Exception e )
        {
            notifyObservers( e );
            LOG.error( e.getMessage(), e );
        }
    }
}
