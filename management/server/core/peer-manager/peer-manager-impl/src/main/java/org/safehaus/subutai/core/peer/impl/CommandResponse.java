package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.Response;


public class CommandResponse
{
    private Response response;
    private CommandResult commandResult;


    public CommandResponse( final Response response, final CommandResult commandResult )
    {
        this.response = response;
        this.commandResult = commandResult;
    }


    public Response getResponse()
    {
        return response;
    }


    public CommandResult getCommandResult()
    {
        return commandResult;
    }
}
