package io.subutai.core.peer.impl.command;


import java.util.UUID;

import io.subutai.common.command.RequestBuilder;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private String hostId;
    private UUID requestId;
    private String environmentId;


    public CommandRequest( final RequestBuilder requestBuilder, final String hostId, final String environmentId )
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


    public String getHostId()
    {
        return hostId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
