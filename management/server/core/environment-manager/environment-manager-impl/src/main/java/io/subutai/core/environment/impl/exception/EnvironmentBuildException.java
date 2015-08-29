package io.subutai.core.environment.impl.exception;


/**
 * Environment build exception. Occurs if some conditions don't apply
 * for certain environment build operations.
 *
 * @see io.subutai.core.env.impl.builder.EnvironmentBuilder
 */
public class EnvironmentBuildException extends Exception
{
    public EnvironmentBuildException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
