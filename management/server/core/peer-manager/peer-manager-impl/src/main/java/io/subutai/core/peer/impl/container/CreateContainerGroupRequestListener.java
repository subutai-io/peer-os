package io.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.common.environment.CreateContainerGroupRequest;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.RecipientType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateContainerGroupRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateContainerGroupRequestListener.class.getName() );

    private LocalPeer localPeer;


    public CreateContainerGroupRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CREATE_CONTAINER_GROUP_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( Payload payload ) throws PeerException
    {
        CreateContainerGroupRequest request = payload.getMessage( CreateContainerGroupRequest.class );
        if ( request != null )
        {

            Set<HostInfoModel> containerHosts = localPeer.createContainerGroup( request );

            return new CreateContainerGroupResponse( containerHosts );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
