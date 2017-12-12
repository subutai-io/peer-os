package io.subutai.hub.share.dto.environment.container;


import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.subutai.hub.share.dto.environment.ContainerStateDto;


public class ContainerDesktopInfoDto
{
    private String id;

    private String type;

    private String rdServer;


    public ContainerDesktopInfoDto()
    {
    }


    public ContainerDesktopInfoDto( final String id, final String type, final String rdServer )
    {
        this.id = id;
        this.type = type;
        this.rdServer = rdServer;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getType()
    {
        return type;
    }


    public void setType( final String type )
    {
        this.type = type;
    }


    public String getRdServer()
    {
        return rdServer;
    }


    public void setRdServer( final String rdServer )
    {
        this.rdServer = rdServer;
    }
}

