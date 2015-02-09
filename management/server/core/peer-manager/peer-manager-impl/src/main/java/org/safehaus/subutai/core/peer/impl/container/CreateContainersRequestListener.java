package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateContainersRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateContainersRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateContainersRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CONTAINER_CREATE_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( Payload payload ) throws PeerException
    {
        CreateContainersRequest request = payload.getMessage( CreateContainersRequest.class );
        if ( request != null )
        {

            Set<HostInfoModel> containerHosts = localPeer
                    .createContainers( request.getEnvironmentId(), request.getInitiatorPeerId(), request.getOwnerId(),
                            request.getTemplates(), request.getNumberOfContainers(), request.getStrategyId(),
                            request.getCriteria() );

            return new CreateContainersResponse( containerHosts );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
