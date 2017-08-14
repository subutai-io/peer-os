package io.subutai.core.hubmanager.api.model;


import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import io.subutai.hub.share.dto.metrics.CpuDto;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.MemoryDto;
import io.subutai.hub.share.dto.metrics.NetDto;


public interface ContainerMetrics
{
    long getId();

    void setId( long id );

    public String getHostId();

    public void setHostId( final String hostId );

    public String getHostName();

    public void setHostName( final String hostName );

    public String getMemory();

    public MemoryDto getMemoryDto();

    public void setMemory( final String memory );

    public void setMemory( final MemoryDto memory );

    public String getCpu();

    public CpuDto getCpuDto();

    public void setCpu( final String cpu );

    public void setCpu( final CpuDto cpu );

    public String getNet();

    public Map<String, NetDto> getNetDto();

    public void setNet( final String net );

    public void setNet( final Map<String, NetDto> net );

    public String getDisk();

    public Map<String, DiskDto> getDiskDto();

    public void setDisk( final String disk );

    public void setDisk( final Map<String, DiskDto> disk );

    public Date getStartTime();

    public void setStartTime( final Date startTime );

    public Date getEndTime();

    public void setEndTime( final Date endTime );
}
