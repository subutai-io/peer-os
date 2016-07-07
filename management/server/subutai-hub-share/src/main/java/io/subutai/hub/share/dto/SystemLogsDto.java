package io.subutai.hub.share.dto;


import java.util.Set;


public class SystemLogsDto
{
    private Set<String> logs;
    private String status;


    public SystemLogsDto()
    {
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
}
