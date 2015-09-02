package io.subutai.core.security.broker;


import io.subutai.common.command.RequestBuilder;


public class RequestWrapper
{
    private RequestBuilder.RequestImpl request;


    public RequestBuilder.RequestImpl getRequest()
    {
        return request;
    }
}
