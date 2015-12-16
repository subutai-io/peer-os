package io.subutai.core.environment.api.exception;


/**
 * Thrown if general error occurred in environment manager
 */
public class EnvironmentManagerException extends Exception
{
    public EnvironmentManagerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public EnvironmentManagerException( final String s )
    {
        super(s);
    }
}
