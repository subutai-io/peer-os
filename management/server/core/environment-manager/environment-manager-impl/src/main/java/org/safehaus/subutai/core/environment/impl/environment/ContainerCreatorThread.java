package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;
import java.util.Set;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerCreatorThread extends Observable implements Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerCreatorThread.class.getName() );
    EnvironmentBuilderImpl builder;
    private CloneContainersMessage message;
    private PeerManager peerManager;


    public ContainerCreatorThread( final EnvironmentBuilderImpl environmentBuilder,
                                   final CloneContainersMessage message, final PeerManager peerManager )
    {
        this.builder = environmentBuilder;
        this.message = message;
        this.peerManager = peerManager;
    }


    @Override
    public void run()
    {
        try
        {
            Set<ContainerHost> containers = peerManager.getPeer( peerManager.getLocalPeer().getId() ).
                    createContainers( message.getPeerId(), message.getEnvId(), message.getTemplates(),
                            message.getNumberOfNodes(), message.getStrategy(), null, message.getNodeGroupName() );
            builder.update( this, containers );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            builder.update( this, e );
        }
    }
}
