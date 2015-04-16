package org.safehaus.subutai.core.env.api.exception;


/**
 * Thrown if error occurred in environment maanger
 */
public class EnvironmentManagerException extends Exception
{
    public EnvironmentManagerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
