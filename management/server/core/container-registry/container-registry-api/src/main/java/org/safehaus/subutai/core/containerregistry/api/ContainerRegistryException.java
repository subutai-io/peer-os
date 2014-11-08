package org.safehaus.subutai.core.containerregistry.api;


/**
 * Exception thrown by ContainerRegistry methods
 */
public class ContainerRegistryException extends Exception
{
    public ContainerRegistryException( final String message )
    {
        super( message );
    }


    public ContainerRegistryException( final Throwable cause )
    {
        super( cause );
    }
}
