package org.safehaus.subutai.core.peer.impl;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private ContainerHost host;
    private Date createDate;
    private UUID requestId;


    public CommandRequest( final RequestBuilder requestBuilder, final ContainerHost host )
    {
        this.requestBuilder = requestBuilder;
        this.host = host;
        this.createDate = new Date();
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


    public Date getCreateDate()
    {
        return createDate;
    }
}
