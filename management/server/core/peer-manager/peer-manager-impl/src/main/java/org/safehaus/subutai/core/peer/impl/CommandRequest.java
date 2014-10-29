package org.safehaus.subutai.core.peer.impl;


import java.util.Date;

import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.peer.api.Host;


public class CommandRequest
{
    private RequestBuilder requestBuilder;
    private Host host;
    private Date createDate;


    public CommandRequest( final RequestBuilder requestBuilder, final Host host )
    {
        this.requestBuilder = requestBuilder;
        this.host = host;
        this.createDate = new Date();
    }


    public RequestBuilder getRequestBuilder()
    {
        return requestBuilder;
    }


    public Host getHost()
    {
        return host;
    }


    public Date getCreateDate()
    {
        return createDate;
    }
}
