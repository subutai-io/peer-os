package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;
import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerCreatorThread extends Observable implements Runnable
{

    private ContainerDistributionMessage message;
    private PeerManager peerManager;


    public ContainerCreatorThread( final ContainerDistributionMessage message, final PeerManager peerManager )
    {
        this.message = message;
        this.peerManager = peerManager;
    }


    @Override
    public void run()
    {
        try
        {
            Set<ContainerHost> containers = peerManager.getPeer( message.getSourcePeerId() ).
                    createContainers( message.targetPeerId(), message.getEnvironmentId(), message.getTemplates(),
                            message.getNumberOfContainers(), message.getPlacementStrategy(), null );
//            environmentBuilder.update( this, containers );
        }
        catch ( PeerException e )
        {
            e.printStackTrace();
        }
    }
}
