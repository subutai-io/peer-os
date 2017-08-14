package io.subutai.core.hubmanager.impl.model;


import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.core.hubmanager.api.model.ContainerMetrics;
import io.subutai.hub.share.dto.metrics.CpuDto;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.MemoryDto;
import io.subutai.hub.share.dto.metrics.NetDto;


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

    @Column( name = "host_name" )
    private String hostName;

    @Column( name = "host_memory" )
    private String memory;

    @Column( name = "host_cpu" )
    private String cpu;

    @Column( name = "host_net" )
    private String net;

    @Column( name = "host_disk" )
    private String disk;

    @Column( name = "start_time" )
    private Date startTime = null;

    @Column( name = "end_time" )
    private Date endTime = null;


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


    public String getMemory()
    {
        return memory;
    }


    @Override
    public MemoryDto getMemoryDto()
    {
        Gson gson = new Gson();
        return gson.fromJson( this.memory, MemoryDto.class );
    }


    public void setMemory( final String memory )
    {
        this.memory = memory;
    }


    @Override
    public void setMemory( final MemoryDto memory )
    {
        Gson gson = new Gson();
        this.memory = gson.toJson( memory );
    }


    public String getCpu()
    {
        return cpu;
    }


    @Override
    public CpuDto getCpuDto()
    {
        Gson gson = new Gson();
        return gson.fromJson( this.cpu, CpuDto.class );
    }


    public void setCpu( final String cpu )
    {
        this.cpu = cpu;
    }


    @Override
    public void setCpu( final CpuDto cpu )
    {
        Gson gson = new Gson();
        this.cpu = gson.toJson( cpu );
    }


    public String getNet()
    {
        return net;
    }


    @Override
    public Map<String, NetDto> getNetDto()
    {
        Gson gson = new Gson();
        Type typeOfHashMap = new TypeToken<Map<String, NetDto>>(){}.getType();
        return gson.fromJson( this.net, typeOfHashMap );
    }


    public void setNet( final String net )
    {
        this.net = net;
    }


    @Override
    public void setNet( final Map<String, NetDto> net )
    {
        Gson gson = new Gson();
        this.net = gson.toJson( net );
    }


    public String getDisk()
    {
        return disk;
    }


    @Override
    public Map<String, DiskDto> getDiskDto()
    {
        Gson gson = new Gson();
        Type typeOfHashMap = new TypeToken<Map<String, DiskDto>>(){}.getType();
        return gson.fromJson( this.disk, typeOfHashMap );
    }


    public void setDisk( final String disk )
    {
        this.disk = disk;
    }


    @Override
    public void setDisk( final Map<String, DiskDto> disk )
    {
        Gson gson = new Gson();
        this.disk = gson.toJson( disk );
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
