package org.safehaus.subutai.core.peer.impl;


import java.util.Set;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateContainerRequestListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateContainerRequestListener.class.getName() );

    private LocalPeer localPeer;
    private Messenger messenger;
    private PeerManager peerManager;


    protected CreateContainerRequestListener( LocalPeer localPeer, Messenger messenger, PeerManager peerManager )
    {
        super( RecipientType.CONTAINER_CREATE_REQUEST.name() );

        this.localPeer = localPeer;
        this.messenger = messenger;
        this.peerManager = peerManager;
    }


    @Override
    public void onMessage( final Message message )
    {
        CreateContainerRequest request = message.getPayload( CreateContainerRequest.class );
        try
        {
            Set<ContainerHost> containerHosts = localPeer
                    .createContainers( request.getCreatorPeerId(), request.getEnvironmentId(), request.getTemplates(),
                            request.getQuantity(), request.getStrategyId(), request.getCriteria() );

            Message response =
                    messenger.createMessage( new CreateContainerResponse( request.getRequestId(), containerHosts ) );

            Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );

            messenger.sendMessage( sourcePeer, response, RecipientType.CONTAINER_CREATE_RESPONSE.name(),
                    Timeouts.CREATE_CONTAINER_RESPONSE_TIMEOUT );
        }
        catch ( MessageException | ContainerCreateException e )
        {
            LOG.error( "'Error in CreateContainerRequestListener.onMessage", e );
        }
    }
}
