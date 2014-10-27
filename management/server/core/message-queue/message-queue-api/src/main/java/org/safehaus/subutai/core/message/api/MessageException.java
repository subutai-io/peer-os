package org.safehaus.subutai.core.message.api;


/**
 * Exception thrown by Queue methods
 */
public class MessageException extends Exception
{
    public MessageException( final String message )
    {
        super( message );
    }


    public MessageException( final Throwable cause )
    {
        super( cause );
    }
}
