package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.common.peer.PeerException;
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
    public Object onRequest( Payload payload ) throws PeerException
    {
        CreateContainerRequest request = payload.getMessage( CreateContainerRequest.class );
        if ( request != null )
        {

            Set<HostInfoModel> containerHosts = localPeer
                    .scheduleCloneContainers( request.getCreatorPeerId(), request.getTemplates(), request.getQuantity(),
                            request.getStrategyId(), request.getCriteria() );

            return new CreateContainerResponse( containerHosts );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
