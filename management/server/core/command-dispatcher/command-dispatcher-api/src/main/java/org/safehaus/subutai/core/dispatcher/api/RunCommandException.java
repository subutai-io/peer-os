package org.safehaus.subutai.core.dispatcher.api;


/**
 * Exception that might be thrown by CommandDispatcher
 */
public class RunCommandException extends RuntimeException
{
    public RunCommandException( final String message )
    {
        super( message );
    }
}
