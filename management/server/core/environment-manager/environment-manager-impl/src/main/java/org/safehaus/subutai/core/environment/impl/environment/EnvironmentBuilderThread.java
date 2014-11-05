package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;


/**
 * Created by bahadyr on 11/5/14.
 */
public class EnvironmentBuilderThread extends Observable implements Runnable
{
    EnvironmentBuilderImpl environmentBuilder;
    List<ContainerDistributionMessage> messages;
    PeerManager peerManager;


    public EnvironmentBuilderThread( final EnvironmentBuilderImpl environmentBuilder,
                                     final List<ContainerDistributionMessage> messages, PeerManager peerManager )
    {
        this.environmentBuilder = environmentBuilder;
        this.messages = messages;
        this.peerManager = peerManager;
    }


    @Override
    public void run()
    {
        for ( ContainerDistributionMessage message : messages )
        {
            try
            {
                Set<ContainerHost> containers = peerManager.getPeer( message.getSourcePeerId() ).
                        createContainers( message.targetPeerId(), message.getEnvironmentId(), message.getTemplates(),
                                message.getNumberOfContainers(), message.getPlacementStrategy(), null );
                environmentBuilder.update( this, containers );
            }
            catch ( PeerException e )
            {
                e.printStackTrace();
            }
        }
    }
}
