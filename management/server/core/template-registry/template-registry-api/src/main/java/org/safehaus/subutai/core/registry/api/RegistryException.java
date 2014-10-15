package org.safehaus.subutai.core.registry.api;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Represents exception thrown by some methods of {@code TemplateRegistryManager}
 */
public class RegistryException extends SubutaiException
{

    public RegistryException( final String message )
    {
        super( message );
    }
}
