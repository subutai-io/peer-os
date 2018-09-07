package io.subutai.bazaar.share.dto.metrics;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ContainersMetricsDto
{
    @JsonProperty( "peerId" )
    private String peerId;

    @JsonProperty( "containerHostMetricsDto" )
    private List<HostMetricsDto> containerHostMetricsDto = new ArrayList<>();


    public ContainersMetricsDto( final String peerId )
    {
        this.peerId = peerId;
    }


    public ContainersMetricsDto()
    {
    }


    public String getPeerId()
    {
        return peerId;
    }


    public List<HostMetricsDto> getContainerHostMetricsDto()
    {
        return containerHostMetricsDto;
    }
}
