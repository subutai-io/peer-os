package io.subutai.core.peer.impl.command;


import java.util.UUID;

import org.safehaus.subutai.common.command.RequestBuilder;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private UUID hostId;
    private UUID requestId;
    private UUID environmentId;


    public CommandRequest( final RequestBuilder requestBuilder, final UUID hostId, final UUID environmentId )
    {
        this.requestBuilder = requestBuilder;
        this.hostId = hostId;
        this.environmentId = environmentId;
        this.requestId = UUID.randomUUID();
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public RequestBuilder getRequestBuilder()
    {
        return requestBuilder;
    }


    public UUID getHostId()
    {
        return hostId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }
}
