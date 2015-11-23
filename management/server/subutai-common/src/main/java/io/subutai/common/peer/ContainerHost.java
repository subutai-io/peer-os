package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host, ContainerHostInfo
{
    ContainerId getContainerId();

    String getInitiatorPeerId();

    String getOwnerId();

    String getEnvironmentId();

    public String getNodeGroupName();

    public void dispose() throws PeerException;

    public void start() throws PeerException;

    public void stop() throws PeerException;

    public Peer getPeer();

    public Template getTemplate() throws PeerException;

    public String getTemplateName();

    public void addTag( String tag );

    public void removeTag( String tag );

    public Set<String> getTags();

    public void setDefaultGateway( String gatewayIp ) throws PeerException;

    public boolean isLocal();

    /**
     * Returns process's resource usage by pid
     *
     * @param processPid - pid which process usage to return
     *
     * @return - resource usage
     */
    public ProcessResourceUsage getProcessResourceUsage( int processPid ) throws PeerException;

    /**
     * Returns RAM quota on container in megabytes
     *
     * @return - quota in mb
     */
//    public int getRamQuota() throws PeerException;


    /**
     * Get RAM quota object in details
     *
     * @return - MemoryQuotaInfo carries ram quota specific info
     */
    public RamQuota getRamQuota() throws PeerException;


    /**
     * Sets RAM quota on container in megabytes
     *
     * @param ramInMb - quota in mb
     */
    public void setRamQuota( int ramInMb ) throws PeerException;


    /**
     * Returns CPU quota on container in percent
     *
     * @return - cpu quota on container in percent
     */
//    public int getCpuQuota() throws PeerException;


    /**
     * Returns CPU quota object on container
     *
     * @return - cpu quota object on container
     */
    public CpuQuota getCpuQuota() throws PeerException;

    /**
     * Sets CPU quota on container in percent
     *
     * @param cpuPercent - cpu quota in percent
     */
    public void setCpuQuota( int cpuPercent ) throws PeerException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet() throws PeerException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( Set<Integer> cpuSet ) throws PeerException;

    /**
     * Returns disk quota
     *
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getDiskQuota( DiskPartition diskPartition ) throws PeerException;

    /**
     * Sets disk partition quota
     *
     * @param diskQuota - quota to set
     */
    public void setDiskQuota( DiskQuota diskQuota ) throws PeerException;

    /**
     * Returns available RAM quota on container in megabytes
     *
     * @return - quota in mb
     */
    public RamQuota getAvailableRamQuota() throws PeerException;

    /**
     * Returns available CPU quota on container in percent
     *
     * @return - cpu quota on container in percent
     */
    public CpuQuota getAvailableCpuQuota() throws PeerException;

    /**
     * Returns available disk quota
     *
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getAvailableDiskQuota( DiskPartition diskPartition ) throws PeerException;

    /**
     * Sets ram quota
     *
     * @param ramQuotaInfo - quota to set
     */
    public void setRamQuota( RamQuota ramQuotaInfo ) throws PeerException;
}
