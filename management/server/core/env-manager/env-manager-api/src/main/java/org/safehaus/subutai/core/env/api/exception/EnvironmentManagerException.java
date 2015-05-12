package org.safehaus.subutai.core.env.api.exception;


/**
 * Thrown if general error occurred in environment manager
 */
public class EnvironmentManagerException extends Exception
{
    public EnvironmentManagerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
