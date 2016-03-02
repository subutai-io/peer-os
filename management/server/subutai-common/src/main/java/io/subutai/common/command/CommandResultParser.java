package io.subutai.common.command;


/**
 * Command result parser
 */
public interface CommandResultParser<T>
{
    T parse( final CommandResult commandResult ) throws CommandResultParseException;
}
