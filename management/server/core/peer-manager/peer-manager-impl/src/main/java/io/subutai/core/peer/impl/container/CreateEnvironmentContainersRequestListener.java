package io.subutai.core.peer.impl.container;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.RecipientType;


public class CreateEnvironmentContainersRequestListener extends RequestListener
{

    private static final Logger LOG =
            LoggerFactory.getLogger( CreateEnvironmentContainersRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateEnvironmentContainersRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CREATE_ENVIRONMENT_CONTAINERS_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        CreateEnvironmentContainersRequest request = payload.getMessage( CreateEnvironmentContainersRequest.class );
        if ( request != null )
        {

            Set<HostInfoModel> containerHosts = localPeer.createEnvironmentContainers( request );

            return new CreateContainerGroupResponse( containerHosts );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
