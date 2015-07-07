package io.subutai.core.messenger.api;


/**
 * Exception thrown by Messenger methods
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
