package io.subutai.common.command;


/**
 * Command result parse exception
 */
public class CommandResultParseException extends Exception
{
    public CommandResultParseException( final String message )
    {
        super( message );
    }


    public CommandResultParseException( final Throwable cause )
    {
        super( cause );
    }
}
