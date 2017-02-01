package io.subutai.common.command;


import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CommandResponse
{
    @JsonProperty( value = "response" )
    private ResponseImpl response;

    @JsonProperty( value = "commandResult" )
    private CommandResultImpl commandResult;

    @JsonProperty( value = "requestId" )
    private UUID requestId;


    public CommandResponse( @JsonProperty( value = "requestId" ) final UUID requestId,
                            @JsonProperty( value = "response" ) final ResponseImpl response,
                            @JsonProperty( value = "commandResult" ) final CommandResultImpl commandResult )
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
