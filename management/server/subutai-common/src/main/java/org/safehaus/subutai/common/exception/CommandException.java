package org.safehaus.subutai.common.exception;


/**
 * Exception which might be thrown by Command.execute call
 */
public class CommandException extends Exception
{
    public CommandException( final String message )
    {
        super( message );
    }


    public CommandException( final Throwable cause )
    {
        super( cause );
    }
}
