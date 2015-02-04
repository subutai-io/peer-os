package org.safehaus.subutai.core.peer.api;


public class ResourceHostException extends Exception
{
    @Deprecated
    public ResourceHostException( final String message, final String description )
    {
        super( message );
    }


    public ResourceHostException( final String message )
    {
        super( message );
    }


    public ResourceHostException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
