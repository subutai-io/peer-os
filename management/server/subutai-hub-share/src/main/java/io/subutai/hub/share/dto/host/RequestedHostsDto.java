package io.subutai.hub.share.dto.host;


import java.util.ArrayList;
import java.util.List;


public class RequestedHostsDto
{

    private String peerId;

    private List<RequestedHostDto> requestedHostsDto = new ArrayList<>();


    public RequestedHostsDto()
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


    public List<RequestedHostDto> getRequestedHostsDto()
    {
        return requestedHostsDto;
    }


    public void setRequestedHostsDto( final List<RequestedHostDto> requestedHostsDto )
    {
        this.requestedHostsDto = requestedHostsDto;
    }


    public void addRequestedHostsDto( final RequestedHostDto requestedHostDto )
    {
        this.requestedHostsDto.add( requestedHostDto );
    }
}
