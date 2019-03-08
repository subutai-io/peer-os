package io.subutai.common.peer;


import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

import io.subutai.common.util.JsonUtil;


public class Payload
{
    protected String request;
    private String sourcePeerId;


    public Payload( final Object request, String sourcePeerId )
    {
        this.request = JsonUtil.toJsonString( request );
        this.sourcePeerId = sourcePeerId;
    }


    public <T> T getMessage( Class<T> clazz )
    {
        if ( !StringUtils.isBlank( request ) )
        {
            return JsonUtil.fromJsonString( request, clazz );
        }
        return null;
    }


    public String getSourcePeerId()
    {
        return sourcePeerId;
    }
}
