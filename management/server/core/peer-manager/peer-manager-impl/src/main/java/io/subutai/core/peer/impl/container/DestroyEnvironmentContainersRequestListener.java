package io.subutai.core.peer.impl.container;


import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.RecipientType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DestroyEnvironmentContainersRequestListener extends RequestListener
{
    private static final Logger LOG =
            LoggerFactory.getLogger( DestroyEnvironmentContainersRequestListener.class.getName() );

    private LocalPeer localPeer;


    public DestroyEnvironmentContainersRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CONTAINER_DESTROY_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        DestroyEnvironmentContainersRequest request = payload.getMessage( DestroyEnvironmentContainersRequest.class );

        if ( request != null )
        {
            ContainersDestructionResult result = localPeer.destroyEnvironmentContainers( request.getEnvironmentId() );

            return new DestroyEnvironmentContainersResponse( result.getDestroyedContainersIds(),
                    result.getException() );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
