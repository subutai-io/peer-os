package io.subutai.hub.share.dto;


import com.fasterxml.jackson.annotation.JsonProperty;


public class ResourceHostProxyDto
{
    @JsonProperty( "data" )
    private String data;


    public ResourceHostProxyDto()
    {
    }


    public ResourceHostProxyDto( String data )
    {
        this.data = data;
    }


    public String getData()
    {
        return data;
    }


    public void setData( final String data )
    {
        this.data = data;
    }
}
