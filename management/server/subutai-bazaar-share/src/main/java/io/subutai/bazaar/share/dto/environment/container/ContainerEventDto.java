package io.subutai.bazaar.share.dto.environment.container;


import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.subutai.bazaar.share.dto.environment.ContainerStateDto;


public class ContainerEventDto
{
    private String id;

    private Date time;

    private String containerId;

    private String envId;

    private ContainerStateDto state;

    private ContainerDesktopInfoDto desktopInfo;


    public ContainerEventDto()
    {
    }


    public ContainerEventDto( String containerId, String envId, ContainerStateDto state )
    {
        id = RandomStringUtils.randomAlphabetic( 12 );
        time = new Date();

        this.containerId = containerId;
        this.envId = envId;
        this.state = state;
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


    public ContainerStateDto getState()
    {
        return state;
    }


    public void setState( final ContainerStateDto state )
    {
        this.state = state;
    }


    public Date getTime()
    {
        return time;
    }


    public ContainerDesktopInfoDto getDesktopInfo()
    {
        return desktopInfo;
    }


    public void setDesktopInfo( final ContainerDesktopInfoDto desktopInfo )
    {
        this.desktopInfo = desktopInfo;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "id", id ).append( "time", time )
                                                                            .append( "containerId", containerId )
                                                                            .append( "envId", envId )
                                                                            .append( "state", state ).toString();
    }
}

