package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.util.JsonUtil;

import com.google.common.base.Strings;


public class Payload
{
    private String request;


    public Payload( final Object request )
    {
        this.request = JsonUtil.toJson( request );
    }


    public <T> T getMessage( Class<T> clazz )
    {
        if ( !Strings.isNullOrEmpty( request ) )
        {
            return JsonUtil.fromJson( request, clazz );
        }
        return null;
    }
}
