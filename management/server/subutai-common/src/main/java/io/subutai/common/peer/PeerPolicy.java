package io.subutai.common.peer;


import java.util.Objects;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;


@SuppressWarnings( "unused" )
public class PeerPolicy
{
    @JsonProperty( "peerId" )
    private String peerId;

    @JsonProperty( "diskUsageLimit" )
    private int diskUsageLimit;

    @JsonProperty( "cpuUsageLimit" )
    private int cpuUsageLimit;

    @JsonProperty( "memoryUsageLimit" )
    private int memoryUsageLimit;

    @JsonProperty( "networkUsageLimit" )
    private int networkUsageLimit;

    @JsonProperty( "environmentLimit" )
    private int environmentLimit;

    @JsonProperty( "containerLimit" )
    private int containerLimit;


    public PeerPolicy( @JsonProperty( "peerId" ) final String peerId,
                       @JsonProperty( "diskUsageLimit" ) final int diskUsageLimit,
                       @JsonProperty( "cpuUsageLimit" ) final int cpuUsageLimit,
                       @JsonProperty( "memoryUsageLimit" ) final int memoryUsageLimit,
                       @JsonProperty( "networkUsageLimit" ) final int networkUsageLimit,
                       @JsonProperty( "environmentLimit" ) final int environmentLimit,
                       @JsonProperty( "containerLimit" ) final int containerLimit )
    {
        Preconditions.checkArgument( diskUsageLimit >= 0 );
        Preconditions.checkArgument( cpuUsageLimit >= 0 );
        Preconditions.checkArgument( memoryUsageLimit >= 0 );
        Preconditions.checkArgument( networkUsageLimit >= 0 );
        Preconditions.checkArgument( environmentLimit >= 0 );
        Preconditions.checkArgument( containerLimit >= 0 );
        this.peerId = peerId;
        this.diskUsageLimit = diskUsageLimit;
        this.cpuUsageLimit = cpuUsageLimit;
        this.memoryUsageLimit = memoryUsageLimit;
        this.networkUsageLimit = networkUsageLimit;
        this.environmentLimit = environmentLimit;
        this.containerLimit = containerLimit;
    }


    public int getDiskUsageLimit()
    {
        return diskUsageLimit;
    }


    public int getCpuUsageLimit()
    {
        return cpuUsageLimit;
    }


    public int getContainerLimit()
    {
        return containerLimit;
    }


    public int getMemoryUsageLimit()
    {
        return memoryUsageLimit;
    }


    public int getNetworkUsageLimit()
    {
        return networkUsageLimit;
    }


    public int getEnvironmentLimit()
    {
        return environmentLimit;
    }


    public void setContainerLimit( int containerLimit )
    {
        if ( containerLimit < 0 )
        {
            throw new IllegalArgumentException( "Container limit could not be less than 0." );
        }
        this.containerLimit = containerLimit;
    }


    public void setCpuUsageLimit( int cpuUsageLimit )
    {
        if ( cpuUsageLimit < 0 || cpuUsageLimit > 100 )
        {
            throw new IllegalArgumentException( "Invalid CPU usage limit." );
        }
        this.cpuUsageLimit = cpuUsageLimit;
    }


    public void setDiskUsageLimit( int diskUsageLimit )
    {
        if ( diskUsageLimit < 0 || diskUsageLimit > 100 )
        {
            throw new IllegalArgumentException( "Invalid disk usage limit." );
        }
        this.diskUsageLimit = diskUsageLimit;
    }


    public void setMemoryUsageLimit( int memoryUsageLimit )
    {
        if ( memoryUsageLimit < 0 || memoryUsageLimit > 100 )
        {
            throw new IllegalArgumentException( "Invalid memory usage limit." );
        }
        this.memoryUsageLimit = memoryUsageLimit;
    }


    public void setNetworkUsageLimit( int networkUsageLimit )
    {
        if ( networkUsageLimit < 0 || networkUsageLimit > 100 )
        {
            throw new IllegalArgumentException( "Invalid network usage limit." );
        }
        this.networkUsageLimit = networkUsageLimit;
    }


    public void setEnvironmentLimit( int environmentLimit )
    {
        if ( environmentLimit < 0 )
        {
            throw new IllegalArgumentException( "Environment limit could not be less than 0." );
        }
        this.environmentLimit = environmentLimit;
    }


    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerPolicy )
        {
            PeerPolicy other = ( PeerPolicy ) obj;
            return Objects.equals( this.getPeerId(), other.getPeerId() );
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        int result = peerId.hashCode();
        result = 31 * result + diskUsageLimit;
        result = 31 * result + cpuUsageLimit;
        result = 31 * result + memoryUsageLimit;
        result = 31 * result + networkUsageLimit;
        result = 31 * result + environmentLimit;
        result = 31 * result + containerLimit;
        return result;
    }
}
