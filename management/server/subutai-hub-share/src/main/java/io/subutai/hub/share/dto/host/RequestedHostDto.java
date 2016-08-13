package io.subutai.hub.share.dto.host;

public class RequestedHostDto
{
    public enum Status
    {
        APPROVED, REJECTED, REMOVED
    }

    private String id;

    private String hostname;

    private Status state;


    public RequestedHostDto()
    {
    }


    public RequestedHostDto( final String id, final String hostname, final Status state )
    {
        this.id = id;
        this.hostname = hostname;
        this.state = state;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public Status getState()
    {
        return state;
    }


    public void setState( final Status state )
    {
        this.state = state;
    }
}