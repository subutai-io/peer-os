package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.Response;


public class CommandResponse
{
    private Response response;
    private CommandResult commandResult;
    private UUID requestId;


    public CommandResponse( final UUID requestId, final Response response, final CommandResult commandResult )
    {
        this.requestId = requestId;
        this.response = response;
        this.commandResult = commandResult;
    }


    public UUID getRequestId()
    {
        return requestId;
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
