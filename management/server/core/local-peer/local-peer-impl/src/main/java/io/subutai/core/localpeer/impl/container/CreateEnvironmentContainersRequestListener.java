package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RequestListener;


public class CreateEnvironmentContainersRequestListener extends RequestListener
{

    private static final Logger LOG =
            LoggerFactory.getLogger( CreateEnvironmentContainersRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateEnvironmentContainersRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CREATE_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws PeerException
    {
        CreateEnvironmentContainersRequest request = payload.getMessage( CreateEnvironmentContainersRequest.class );

        if ( request != null )
        {

            return localPeer.createEnvironmentContainers( request );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
