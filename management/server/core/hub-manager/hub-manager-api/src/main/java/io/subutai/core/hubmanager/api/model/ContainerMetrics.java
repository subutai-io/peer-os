package io.subutai.core.hubmanager.api.model;


import java.io.Serializable;
import java.util.Date;


public interface ContainerMetrics
{
    long getId();

    void setId( long id );

    public String getHostId();

    public void setHostId( final String hostId );

    public String getHostName();

    public void setHostName( final String hostName );

    public double getMemory();

    public void setMemory( final double memory );

    public double getCpu();

    public void setCpu( final double cpu );

    public double getNet();

    public void setNet( final double net );

    public double getDisk();

    public void setDisk( final double disk );

    public Date getStartTime();

    public void setStartTime( final Date startTime );

    public Date getEndTime();

    public void setEndTime( final Date endTime );
}
