package io.subutai.hub.share.dto;


import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;


public class ContainerEventDto
{
    public enum Type
    {
        RUNNING
    }


    private String id;

    private Date time;

    private String containerId;

    private Type type;


    public ContainerEventDto()
    {
    }


    public ContainerEventDto( String containerId, Type type )
    {
        id = RandomStringUtils.randomAlphabetic( 12 );
        time = new Date();

        this.containerId = containerId;
        this.type = type;
    }


    public String getId()
    {
        return id;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public Type getType()
    {
        return type;
    }


    public Date getTime()
    {
        return time;
    }
}

