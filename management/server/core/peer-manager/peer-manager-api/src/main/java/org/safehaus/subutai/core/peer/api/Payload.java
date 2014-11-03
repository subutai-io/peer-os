package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.util.JsonUtil;


public class Payload
{
    private String request;


    public Payload( final Object request )
    {
        this.request = JsonUtil.toJson( request );
    }


    public <T> T getMessage( Class<T> clazz )
    {
        return JsonUtil.fromJson( request, clazz );
    }
}
