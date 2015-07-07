package io.subutai.core.hostregistry.api;


/**
 * Exception thrown by ContainerRegistry methods
 */
public class HostRegistryException extends Exception
{
    public HostRegistryException( final Throwable cause )
    {
        super( cause );
    }
}
