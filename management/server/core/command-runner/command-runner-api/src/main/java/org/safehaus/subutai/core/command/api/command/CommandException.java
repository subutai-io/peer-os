package org.safehaus.subutai.core.command.api.command;


/**
 * Exception which might be thrown by Command.execute call
 */
public class CommandException extends Exception
{
    public CommandException( final String message )
    {
        super( message );
    }
}
