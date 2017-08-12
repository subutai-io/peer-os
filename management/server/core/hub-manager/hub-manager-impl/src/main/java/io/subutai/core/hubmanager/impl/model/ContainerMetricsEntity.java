package io.subutai.core.hubmanager.impl.model;


import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.hubmanager.api.model.ContainerMetrics;


/**
 * User for container metrics, which are not sent to Hub.
 */
@Entity
@Table( name = "container_metrics" )
@Access( AccessType.FIELD )
public class ContainerMetricsEntity implements ContainerMetrics
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "host_id" )
    private String hostId;

    @Column( "host_name" )
    private String hostName;

    @Column( "hostMemory" )
    private double memory;

    @Column( "hostCpu" )
    private double cpu;

    @Column( "hostNet" )
    private double net;

    @Column( "hostDisk" )
    private double disk;

    @Column( "start_time" )
    private Date startTime = new Date();

    @Column( "end_time" )
    private Date endTime = new Date();


    @Override
    public long getId()
    {
        return id;
    }


    public void setId( final long id )
    {
        this.id = id;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
    }


    public double getMemory()
    {
        return memory;
    }


    public void setMemory( final double memory )
    {
        this.memory = memory;
    }


    public double getCpu()
    {
        return cpu;
    }


    public void setCpu( final double cpu )
    {
        this.cpu = cpu;
    }


    public double getNet()
    {
        return net;
    }


    public void setNet( final double net )
    {
        this.net = net;
    }


    public double getDisk()
    {
        return disk;
    }


    public void setDisk( final double disk )
    {
        this.disk = disk;
    }


    public Date getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Date startTime )
    {
        this.startTime = startTime;
    }


    public Date getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Date endTime )
    {
        this.endTime = endTime;
    }
}
