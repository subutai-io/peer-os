package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.DestroyEnvironmentContainerGroupRequest;
import io.subutai.common.environment.DestroyEnvironmentContainerGroupResponse;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.RequestListener;


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
