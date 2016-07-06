package io.subutai.hub.share.dto;


import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Deprecated
public class ContainerEventDto
{
    public enum Type
    {
        RUNNING
    }


    private String id;

    private Date time;

    private String containerId;

    private String envId;

    private Type type;


    public ContainerEventDto()
    {
    }


    public ContainerEventDto( String containerId, String envId, Type type )
    {
        id = RandomStringUtils.randomAlphabetic( 12 );
        time = new Date();

        this.containerId = containerId;
        this.envId = envId;
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


    public String getEnvId()
    {
        return envId;
    }


    public Type getType()
    {
        return type;
    }


    public Date getTime()
    {
        return time;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE )
                .append( "id", id )
                .append( "time", time )
                .append( "containerId", containerId )
                .append( "envId", envId )
                .append( "type", type )
                .toString();
    }
}

