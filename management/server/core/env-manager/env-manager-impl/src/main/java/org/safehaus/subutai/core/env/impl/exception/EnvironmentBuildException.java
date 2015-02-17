package org.safehaus.subutai.core.env.impl.exception;


/**
 * Environment build exception. Occurs if some conditions don't apply
 * for certain environment build operations.
 *
 * @see org.safehaus.subutai.core.env.impl.builder.TopologyBuilder
 */
public class EnvironmentBuildException extends Exception
{
    public EnvironmentBuildException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
