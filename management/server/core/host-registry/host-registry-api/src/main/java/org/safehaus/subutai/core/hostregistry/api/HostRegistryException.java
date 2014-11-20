package org.safehaus.subutai.core.hostregistry.api;


/**
 * Exception thrown by ContainerRegistry methods
 */
public class HostRegistryException extends Exception
{
    public HostRegistryException( final String message )
    {
        super( message );
    }


    public HostRegistryException( final Throwable cause )
    {
        super( cause );
    }
}
