package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Response;

import com.google.common.base.Preconditions;


/**
 * Wrapper around Response for sending back to remote peer
 */
public class RemoteResponse
{

    private final UUID commandId;
    private final UUID agentId;
    private final Response response;


    public RemoteResponse( final Response response )
    {
        Preconditions.checkNotNull( response, "Response is null" );

        this.commandId = response.getTaskUuid();
        this.agentId = response.getUuid();
        this.response = response;
    }


    public UUID getAgentId()
    {
        return agentId;
    }


    public UUID getCommandId()
    {
        return commandId;
    }


    public Response getResponse()
    {
        return response;
    }
}
