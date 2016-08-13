package io.subutai.hub.share.dto.host;

public class RequestedHostDto
{
    public enum Status
    {
        REQUESTED, APPROVED, REJECTED
    }

    private String id;

    private String hostname;

    private Status status;

    private Boolean management;

    public RequestedHostDto()
    {
        management = false;
    }


    public RequestedHostDto( final String id, final String hostname, final Status status )
    {
        this.id = id;
        this.hostname = hostname;
        this.status = status;
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


    public Status getStatus()
    {
        return status;
    }


    public void setStatus( final Status state )
    {
        this.status = state;
    }


    public Boolean getManagement()
    {
        return management;
    }


    public void setManagement( final Boolean management )
    {
        this.management = management;
    }
}