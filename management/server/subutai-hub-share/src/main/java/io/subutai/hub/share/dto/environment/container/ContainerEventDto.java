package io.subutai.hub.share.dto.environment.container;


import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.subutai.hub.share.dto.environment.ContainerStateDto;


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

    private ContainerStateDto state;


    public ContainerEventDto()
    {
    }


    public ContainerEventDto( String containerId, String envId, Type type, String state )
    {
        id = RandomStringUtils.randomAlphabetic( 12 );
        time = new Date();

        this.containerId = containerId;
        this.envId = envId;
        this.type = type;
        this.state = ContainerStateDto.valueOf( state );
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


    public ContainerStateDto getState()
    {
        return state;
    }


    public void setState( final ContainerStateDto state )
    {
        this.state = state;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "id", id ).append( "time", time )
                                                                            .append( "containerId", containerId )
                                                                            .append( "envId", envId )
                                                                            .append( "type", type ).toString();
    }
}

