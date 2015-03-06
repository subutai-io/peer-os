package org.safehaus.subutai.common.peer;


import java.util.Objects;
import java.util.UUID;


@SuppressWarnings("unused")
public class PeerPolicy {

    private UUID remotePeerId;
    private int diskUsagePercentageLimit;
    private int cpuUsagePercentageLimit;
    private int memoryUsagePercentageLimit;
    private int networkUsagePercentageLimit;
    private int environmentCountLimit;
    private int containerCountLimit;


    public PeerPolicy( UUID remotePeerId ) {
        this.remotePeerId = remotePeerId;
    }


    public int getDiskUsagePercentageLimit() {
        return diskUsagePercentageLimit;
    }


    public int getCpuUsagePercentageLimit() {
        return cpuUsagePercentageLimit;
    }


    public int getContainerCountLimit() {
        return containerCountLimit;
    }


    public int getMemoryUsagePercentageLimit() {
        return memoryUsagePercentageLimit;
    }


    public int getNetworkUsagePercentageLimit() {
        return networkUsagePercentageLimit;
    }


    public int getEnvironmentCountLimit() {
        return environmentCountLimit;
    }


    public void setContainerCountLimit( int containerCountLimit ) {
        if ( containerCountLimit < 0 ) {
            containerCountLimit = 0;
        }
        this.containerCountLimit = containerCountLimit;
    }


    public void setCpuUsagePercentageLimit( int cpuUsagePercentageLimit ) {
        if ( cpuUsagePercentageLimit > 100 ) {
            cpuUsagePercentageLimit = 100;
        }
        else if ( cpuUsagePercentageLimit < 0 ) {
            cpuUsagePercentageLimit = 0;
        }
        this.cpuUsagePercentageLimit = cpuUsagePercentageLimit;
    }


    public void setDiskUsagePercentageLimit( int diskUsagePercentageLimit ) {
        if ( diskUsagePercentageLimit > 100 ) {
            diskUsagePercentageLimit = 100;
        }
        else if ( diskUsagePercentageLimit < 0 ) {
            diskUsagePercentageLimit = 0;
        }
        this.diskUsagePercentageLimit = diskUsagePercentageLimit;
    }


    public void setMemoryUsagePercentageLimit( int memoryUsagePercentageLimit ) {
        if ( memoryUsagePercentageLimit > 100 ) {
            memoryUsagePercentageLimit = 100;
        }
        else if ( memoryUsagePercentageLimit < 0 ) {
            memoryUsagePercentageLimit = 0;
        }
        this.memoryUsagePercentageLimit = memoryUsagePercentageLimit;
    }


    public void setNetworkUsagePercentageLimit( int networkUsagePercentageLimit ) {
        if ( networkUsagePercentageLimit > 100 ) {
            networkUsagePercentageLimit = 100;
        }
        else if ( networkUsagePercentageLimit < 0 ) {
            networkUsagePercentageLimit = 0;
        }
        this.networkUsagePercentageLimit = networkUsagePercentageLimit;
    }


    public void setEnvironmentCountLimit( int environmentCountLimit ) {
        if ( environmentCountLimit < 0 ) {
            environmentCountLimit = 0;
        }
        this.environmentCountLimit = environmentCountLimit;
    }


    public UUID getRemotePeerId() {
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
}
