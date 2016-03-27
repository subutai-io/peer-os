package io.subutai.hub.share.dto;


import java.util.List;


public class PeerTrafficDataDto
{
    private String peerId;

    private List<PeerTrafficRequestDto> requests;


    public PeerTrafficDataDto()
    {
    }


    public PeerTrafficDataDto( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public List<PeerTrafficRequestDto> getRequests()
    {
        return requests;
    }


    public void setRequests( final List<PeerTrafficRequestDto> requests )
    {
        this.requests = requests;
    }
}
