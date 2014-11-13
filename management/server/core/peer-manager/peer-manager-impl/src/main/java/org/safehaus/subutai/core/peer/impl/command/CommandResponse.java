package org.safehaus.subutai.core.peer.impl.command;


import java.util.UUID;


public class CommandResponse
{
    private ResponseImpl response;
    private CommandResultImpl commandResult;
    private UUID requestId;


    public CommandResponse( final UUID requestId, final ResponseImpl response, final CommandResultImpl commandResult )
    {
        this.requestId = requestId;
        this.response = response;
        this.commandResult = commandResult;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public ResponseImpl getResponse()
    {
        return response;
    }


    public CommandResultImpl getCommandResult()
    {
        return commandResult;
    }
}
