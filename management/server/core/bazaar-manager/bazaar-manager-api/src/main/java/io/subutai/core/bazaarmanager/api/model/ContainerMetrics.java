package io.subutai.core.bazaarmanager.api.model;


import java.util.Date;
import java.util.Map;

import io.subutai.bazaar.share.dto.metrics.CpuDto;
import io.subutai.bazaar.share.dto.metrics.DiskDto;
import io.subutai.bazaar.share.dto.metrics.MemoryDto;
import io.subutai.bazaar.share.dto.metrics.NetDto;


public interface ContainerMetrics
{

    long getId();

    void setId( long id );

    String getHostId();

    void setHostId( final String hostId );

    String getHostName();

    void setHostName( final String hostName );

    MemoryDto getMemoryDto();

    void setMemory( final MemoryDto memory );

    CpuDto getCpuDto();

    void setCpu( final CpuDto cpu );

    Map<String, NetDto> getNetDto();

    void setNet( final Map<String, NetDto> net );

    Map<String, DiskDto> getDiskDto();

    void setDisk( final Map<String, DiskDto> disk );

    Date getStartTime();

    void setStartTime( final Date startTime );

    Date getEndTime();

    void setEndTime( final Date endTime );
}
