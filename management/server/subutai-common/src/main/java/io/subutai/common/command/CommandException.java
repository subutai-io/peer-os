package io.subutai.common.command;


/**
 * Exception which might be thrown by CommandExecutor.execute call variations
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
