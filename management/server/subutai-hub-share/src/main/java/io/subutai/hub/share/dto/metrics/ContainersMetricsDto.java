package io.subutai.hub.share.dto.metrics;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ContainersMetricsDto
{
    @JsonProperty( "peerId" )
    private String peerId;

    @JsonProperty( "hostId" )
    private String hostId;

    @JsonProperty( "startTime" )
    private Date startTime;

    @JsonProperty( "endTime" )
    private Date endTime;

    @JsonProperty( "containerHostMetricsDto" )
    private List<HostMetricsDto> containerHostMetricsDto = new ArrayList<>();


    public ContainersMetricsDto( final String peerId, final String hostId )
    {
        this.peerId = peerId;
        this.hostId = hostId;
    }


    public ContainersMetricsDto()
    {
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public Date getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Date startTime )
    {
        this.startTime = startTime;
    }


    public Date getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Date endTime )
    {
        this.endTime = endTime;
    }


    public List<HostMetricsDto> getContainerHostMetricsDto()
    {
        return containerHostMetricsDto;
    }


    public void setContainerHostMetricsDto( final List<HostMetricsDto> containerHostMetricsDto )
    {
        this.containerHostMetricsDto = containerHostMetricsDto;
    }
}
