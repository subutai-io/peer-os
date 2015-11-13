package io.subutai.core.peer.impl;


public class PeerInitializationError extends RuntimeException
{
    public PeerInitializationError( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
