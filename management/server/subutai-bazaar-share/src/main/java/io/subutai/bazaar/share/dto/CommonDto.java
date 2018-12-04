package io.subutai.bazaar.share.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class CommonDto
{
    byte[] bytes;


    public CommonDto()
    {
    }

    public CommonDto( byte[] bytes )
    {
        this.bytes = bytes;
    }


    public byte[] getBytes()
    {
        return bytes;
    }


    public void setBytes( final byte[] bytes )
    {
        this.bytes = bytes;
    }
}
