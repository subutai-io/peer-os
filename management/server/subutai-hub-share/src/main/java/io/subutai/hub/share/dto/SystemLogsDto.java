package io.subutai.hub.share.dto;


import java.util.List;
import java.util.Set;


public class SystemLogsDto
{
    private Set<SubutaiErrorEvent> subutaiErrorEvents;
    private Set<String> logs;
    private List<P2PDto> P2PInfo;
    private String status;


    public SystemLogsDto()
    {
    }


    public Set<SubutaiErrorEvent> getSubutaiErrorEvents()
    {
        return subutaiErrorEvents;
    }


    public void setSubutaiErrorEvents( final Set<SubutaiErrorEvent> events )
    {
        this.subutaiErrorEvents = events;
    }


    public Set<String> getLogs()
    {
        return logs;
    }


    public void setLogs( final Set<String> logs )
    {
        this.logs = logs;
    }


    public String getStatus()
    {
        return status;
    }


    public void setStatus( final String status )
    {
        this.status = status;
    }


    public List<P2PDto> getP2PInfo()
    {
        return P2PInfo;
    }


    public void setP2PInfo( final List<P2PDto> p2PInfo )
    {
        this.P2PInfo = p2PInfo;
    }



}
