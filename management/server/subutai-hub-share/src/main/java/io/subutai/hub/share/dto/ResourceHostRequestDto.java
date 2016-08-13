package io.subutai.hub.share.dto;


public class ResourceHostRequestDto
{
    private String peerId;

    private String resourceHostId;


    public ResourceHostRequestDto()
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


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public void setResourceHostId( final String resourceHostId )
    {
        this.resourceHostId = resourceHostId;
    }
}
