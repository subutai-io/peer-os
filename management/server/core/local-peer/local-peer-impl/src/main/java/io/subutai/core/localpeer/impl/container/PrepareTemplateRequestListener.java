package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.RequestListener;


public class PrepareTemplateRequestListener extends RequestListener
{

    private static final Logger LOG =
            LoggerFactory.getLogger( PrepareTemplateRequestListener.class.getName() );

    private LocalPeer localPeer;


    public PrepareTemplateRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.CREATE_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        CreateEnvironmentContainerGroupRequest request =
                payload.getMessage( CreateEnvironmentContainerGroupRequest.class );
        if ( request != null )
        {

           /* Set<ContainerHostInfoModel> containerHosts =*/
            localPeer.createEnvironmentContainerGroup( request );

            return null;
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
