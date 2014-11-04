package org.safehaus.subutai.common.exception;


/**
 * Exception that might be thrown by Command Runner
 */
public class RunCommandException extends RuntimeException
{
    public RunCommandException( final String message )
    {
        super( message );
    }
}
