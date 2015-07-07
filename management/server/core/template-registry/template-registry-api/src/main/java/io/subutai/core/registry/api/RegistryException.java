package io.subutai.core.registry.api;


/**
 * Represents exception thrown by some methods of {@code TemplateRegistryManager}
 */
public class RegistryException extends Exception
{

    public RegistryException( final String message )
    {
        super( message );
    }
}
