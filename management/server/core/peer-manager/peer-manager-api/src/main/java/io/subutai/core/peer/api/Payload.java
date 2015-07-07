package io.subutai.core.peer.api;


import java.util.UUID;

import io.subutai.common.util.JsonUtil;

import com.google.common.base.Strings;


public class Payload
{
    protected String request;
    private UUID sourcePeerId;


    public Payload( final Object request, UUID sourcePeerId )
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


    public UUID getSourcePeerId()
    {
        return sourcePeerId;
    }
}
