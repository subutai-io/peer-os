package io.subutai.bazaar.share.dto;


import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class SystemLogsDto
{
    private Set<SubutaiSystemLog> subutaiSystemLogs;
    private List<P2PDto> P2PInfo;


    public SystemLogsDto()
    {
    }


    public Set<SubutaiSystemLog> getSubutaiSystemLogs()
    {
        return subutaiSystemLogs;
    }


    public void setSubutaiSystemLogs( final Set<SubutaiSystemLog> events )
    {
        this.subutaiSystemLogs = events;
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
