package org.safehaus.subutai.core.peer.impl;


import java.util.Date;

import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private ContainerHost host;
    private Date createDate;


    public CommandRequest( final RequestBuilder requestBuilder, final ContainerHost host )
    {
        this.requestBuilder = requestBuilder;
        this.host = host;
        this.createDate = new Date();
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
