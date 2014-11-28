package org.safehaus.subutai.core.peer.api;


public class ResourceHostException extends HostException
{
    public ResourceHostException( final String message, final String description )
    {
        super( message, description );
    }


    public ResourceHostException( final String message )
    {
        super( message, "" );
    }
}
