package io.subutai.common.command;


public interface CommandResultParser<T>
{
    T parse( CommandResult commandResult ) throws CommandResultParseException;
}
