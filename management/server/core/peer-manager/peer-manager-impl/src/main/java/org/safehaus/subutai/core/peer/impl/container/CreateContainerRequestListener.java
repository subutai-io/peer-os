package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.HostInfoModel;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateContainerRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateContainerRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateContainerRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CONTAINER_CREATE_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( Payload payload )
    {
        CreateContainerRequest request = payload.getMessage( CreateContainerRequest.class );
        if ( request != null )
        {
            try
            {
                Set<HostInfoModel> containerHosts = localPeer
                        .scheduleCloneContainers( request.getCreatorPeerId(), request.getTemplates(),
                                request.getQuantity(), request.getStrategyId(), request.getCriteria() );

                return new CreateContainerResponse( containerHosts );
            }
            catch ( PeerException e )
            {
                LOG.error( "'Error in CreateContainerRequestListener.onMessage", e );
            }
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
