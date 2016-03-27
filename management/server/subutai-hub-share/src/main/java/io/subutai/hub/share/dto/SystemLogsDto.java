package io.subutai.hub.share.dto;


import java.util.Set;


public class SystemLogsDto
{
    private Set<String> logs;

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
}
