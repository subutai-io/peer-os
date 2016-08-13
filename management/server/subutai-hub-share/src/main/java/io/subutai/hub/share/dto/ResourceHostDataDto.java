package io.subutai.hub.share.dto;


import io.subutai.hub.share.dto.host.RequestedHostDto;


public class ResourceHostDataDto
{
    public enum State
    {
        APPROVE, REJECT, REMOVE, NO_STATE
    }

    private State state;

    private RequestedHostDto.Status status;

    private String peerId;

    private String resourceHostId;


    public ResourceHostDataDto()
    {
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public void setResourceHostId( final String resourceHostId )
    {
        this.resourceHostId = resourceHostId;
    }


    public RequestedHostDto.Status getStatus()
    {
        return status;
    }


    public void setStatus( final RequestedHostDto.Status status )
    {
        this.status = status;
    }
}
