package io.subutai.common.peer;


import java.util.Objects;


@SuppressWarnings( "unused" )
public class PeerPolicy
{

    private String remotePeerId;
    private int diskUsagePercentageLimit;
    private int cpuUsagePercentageLimit;
    private int memoryUsagePercentageLimit;
    private int networkUsagePercentageLimit;
    private int environmentCountLimit;
    private int containerCountLimit;


    public PeerPolicy( String remotePeerId )
    {
        this.remotePeerId = remotePeerId;
    }


    public int getDiskUsagePercentageLimit()
    {
        return diskUsagePercentageLimit;
    }


    public int getCpuUsagePercentageLimit()
    {
        return cpuUsagePercentageLimit;
    }


    public int getContainerCountLimit()
    {
        return containerCountLimit;
    }


    public int getMemoryUsagePercentageLimit()
    {
        return memoryUsagePercentageLimit;
    }


    public int getNetworkUsagePercentageLimit()
    {
        return networkUsagePercentageLimit;
    }


    public int getEnvironmentCountLimit()
    {
        return environmentCountLimit;
    }


    public void setContainerCountLimit( int containerCountLimit )
    {
        int cCountLimit = containerCountLimit;
        if ( cCountLimit < 0 )
        {
            cCountLimit = 0;
        }
        this.containerCountLimit = cCountLimit;
    }


    public void setCpuUsagePercentageLimit( int cpuUsagePercentageLimit )
    {
        int cpuPercentageLimit = cpuUsagePercentageLimit;
        if ( cpuPercentageLimit > 100 )
        {
            cpuPercentageLimit = 100;
        }
        else if ( cpuPercentageLimit < 0 )
        {
            cpuPercentageLimit = 0;
        }
        this.cpuUsagePercentageLimit = cpuPercentageLimit;
    }


    public void setDiskUsagePercentageLimit( int diskUsagePercentageLimit )
    {
        int diskPercentageLimit = diskUsagePercentageLimit;
        if ( diskPercentageLimit > 100 )
        {
            diskPercentageLimit = 100;
        }
        else if ( diskPercentageLimit < 0 )
        {
            diskPercentageLimit = 0;
        }
        this.diskUsagePercentageLimit = diskPercentageLimit;
    }


    public void setMemoryUsagePercentageLimit( int memoryUsagePercentageLimit )
    {
        int memoryPercentageLimit = memoryUsagePercentageLimit;
        if ( memoryPercentageLimit > 100 )
        {
            memoryPercentageLimit = 100;
        }
        else if ( memoryPercentageLimit < 0 )
        {
            memoryPercentageLimit = 0;
        }
        this.memoryUsagePercentageLimit = memoryPercentageLimit;
    }


    public void setNetworkUsagePercentageLimit( int networkUsagePercentageLimit )
    {
        int networkPercentageLimit = networkUsagePercentageLimit;
        if ( networkPercentageLimit > 100 )
        {
            networkPercentageLimit = 100;
        }
        else if ( networkPercentageLimit < 0 )
        {
            networkPercentageLimit = 0;
        }
        this.networkUsagePercentageLimit = networkPercentageLimit;
    }


    public void setEnvironmentCountLimit( int environmentCountLimit )
    {
        int eCountLimit = environmentCountLimit;
        if ( eCountLimit < 0 )
        {
            eCountLimit = 0;
        }
        this.environmentCountLimit = eCountLimit;
    }


    public String getRemotePeerId()
    {
        return remotePeerId;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerPolicy )
        {
            PeerPolicy other = ( PeerPolicy ) obj;
            return Objects.equals( this.getRemotePeerId(), other.getRemotePeerId() );
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        int result = remotePeerId.hashCode();
        result = 31 * result + diskUsagePercentageLimit;
        result = 31 * result + cpuUsagePercentageLimit;
        result = 31 * result + memoryUsagePercentageLimit;
        result = 31 * result + networkUsagePercentageLimit;
        result = 31 * result + environmentCountLimit;
        result = 31 * result + containerCountLimit;
        return result;
    }
}
