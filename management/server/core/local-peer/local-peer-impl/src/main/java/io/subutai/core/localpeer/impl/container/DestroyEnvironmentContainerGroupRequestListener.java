package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.localpeer.impl.RecipientType;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;


public class DestroyEnvironmentContainerGroupRequestListener extends RequestListener
{
    private static final Logger LOG =
            LoggerFactory.getLogger( DestroyEnvironmentContainerGroupRequestListener.class.getName() );

    private LocalPeer localPeer;


    public DestroyEnvironmentContainerGroupRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.DESTROY_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        DestroyEnvironmentContainerGroupRequest request = payload.getMessage( DestroyEnvironmentContainerGroupRequest.class );

        if ( request != null )
        {
            ContainersDestructionResult result =
                    localPeer.destroyContainersByEnvironment( request.getEnvironmentId() );

            return new DestroyEnvironmentContainerGroupResponse( result.getDestroyedContainersIds(),
                    result.getException() );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
