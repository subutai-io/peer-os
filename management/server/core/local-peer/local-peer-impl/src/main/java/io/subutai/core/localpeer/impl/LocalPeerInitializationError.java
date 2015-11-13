package io.subutai.core.localpeer.impl;



public class LocalPeerInitializationError extends RuntimeException
{
    public LocalPeerInitializationError( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
