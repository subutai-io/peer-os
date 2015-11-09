package io.subutai.common.peer;


import com.google.common.base.Strings;

import io.subutai.common.util.JsonUtil;


public class Payload
{
    protected String request;
    private String sourcePeerId;


    public Payload( final Object request, String sourcePeerId )
    {
        this.request = JsonUtil.toJson( request );
        this.sourcePeerId = sourcePeerId;
    }


    public <T> T getMessage( Class<T> clazz )
    {
        if ( !Strings.isNullOrEmpty( request ) )
        {
            return JsonUtil.fromJson( request, clazz );
        }
        return null;
    }


    public String getSourcePeerId()
    {
        return sourcePeerId;
    }
}
