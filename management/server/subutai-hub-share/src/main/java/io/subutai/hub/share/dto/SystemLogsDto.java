package io.subutai.hub.share.dto;


import java.util.List;
import java.util.Set;


public class SystemLogsDto
{
    private Set<SubutaiErrorEvent> logs;
    private List<P2PDto> P2PInfo;
    private String status;


    public SystemLogsDto()
    {
    }


    public Set<SubutaiErrorEvent> getLogs()
    {
        return logs;
    }


    public void setLogs( final Set<SubutaiErrorEvent> logs )
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
