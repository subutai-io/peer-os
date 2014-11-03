package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.RequestListener;


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
    public Object onRequest( final Object request ) throws Exception
    {
        return request;
    }
}
