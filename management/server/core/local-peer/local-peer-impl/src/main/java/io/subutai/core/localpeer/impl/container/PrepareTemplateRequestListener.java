package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RequestListener;


public class PrepareTemplateRequestListener extends RequestListener
{

    private static final Logger LOG = LoggerFactory.getLogger( PrepareTemplateRequestListener.class.getName() );

    private LocalPeer localPeer;


    public PrepareTemplateRequestListener( LocalPeer localPeer )
    {
        super( RecipientType.PREPARE_TEMPLATE_REQUEST.name() );

        this.localPeer = localPeer;
    }


    @Override
    public Object onRequest( final Payload payload ) throws PeerException
    {
        PrepareTemplatesRequest request = payload.getMessage( PrepareTemplatesRequest.class );

        if ( request != null )
        {
            return localPeer.prepareTemplates( request );
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }
}
