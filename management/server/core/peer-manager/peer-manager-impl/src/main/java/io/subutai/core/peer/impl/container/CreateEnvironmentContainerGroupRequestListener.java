package io.subutai.core.peer.impl.container;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.RecipientType;


public class CreateEnvironmentContainerGroupRequestListener extends RequestListener
{

    private static final Logger LOG =
            LoggerFactory.getLogger( CreateEnvironmentContainerGroupRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateEnvironmentContainerGroupRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CREATE_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        CreateEnvironmentContainerGroupRequest request = payload.getMessage( CreateEnvironmentContainerGroupRequest.class );
        if ( request != null )
        {

            Set<HostInfoModel> containerHosts = localPeer.createEnvironmentContainerGroup( request );

            return new CreateEnvironmentContainerGroupResponse( containerHosts );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
