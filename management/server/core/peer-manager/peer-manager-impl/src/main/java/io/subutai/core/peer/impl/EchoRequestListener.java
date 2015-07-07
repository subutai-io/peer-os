package io.subutai.core.peer.impl;


import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RequestListener;


/**
 * Simple echo request listener for testing purposes
 */
public class EchoRequestListener extends RequestListener
{
    protected EchoRequestListener()
    {
        super( RecipientType.ECHO_LISTENER.name() );
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        return payload.getMessage( String.class );
    }
}
