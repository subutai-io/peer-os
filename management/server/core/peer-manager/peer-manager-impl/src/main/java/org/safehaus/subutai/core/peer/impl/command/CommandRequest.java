package org.safehaus.subutai.core.peer.impl.command;


import java.util.UUID;

import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private ContainerHost host;
    private UUID requestId;


    public CommandRequest( final RequestBuilder requestBuilder, final ContainerHost host )
    {
        this.requestBuilder = requestBuilder;
        this.host = host;
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


    public ContainerHost getHost()
    {
        return host;
    }
}
