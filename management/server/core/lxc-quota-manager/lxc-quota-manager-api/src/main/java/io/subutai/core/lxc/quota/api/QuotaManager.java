package io.subutai.core.lxc.quota.api;


import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.Quota;
import io.subutai.common.quota.QuotaParser;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;


/**
 * Quota Manager Api layer for reading/setting quota on a container host. {@code QuotaManager} identifies its {@code
 * ResourceHost} and executes relevant subutai quota command.
 */
public interface QuotaManager
{
    public static QuotaType[] DEFAULT_QUOTA_TYPES = {
            QuotaType.QUOTA_TYPE_RAM, QuotaType.QUOTA_TYPE_CPU, QuotaType.QUOTA_TYPE_DISK_OPT,
            QuotaType.QUOTA_TYPE_DISK_HOME, QuotaType.QUOTA_TYPE_DISK_VAR, QuotaType.QUOTA_TYPE_DISK_ROOTFS
    };

    DiskQuota getDiskQuota( ContainerId containerId, DiskPartition diskPartition ) throws QuotaException;

    DiskQuota getAvailableDiskQuota( ContainerId containerId, DiskPartition diskPartition )
            throws QuotaException;

    Quota getAvailableQuota( ContainerId containerId, QuotaType quotaType ) throws QuotaException;

    /**
     * Returns quota parser by quota type.
     *
     * @param quotaType quota type
     * @return @see QuotaParser quota parser
     * @throws QuotaException
     */
    QuotaParser getQuotaParser( QuotaType quotaType ) throws QuotaException;

    /**
     * Returns predefined quotas of container type
     *
     * @param containerType @see ContainerType container type
     * @return @see ContainerQuota
     */
    ContainerQuota getDefaultContainerQuota( ContainerType containerType );

    /**
     * Set quota by quota type and quota value represented as string
     *
     * @param containerId container ID
     * @param quotaType quota type
     * @param quotaValue quota value
     * @throws QuotaException
     */
    void setQuota( ContainerId containerId, QuotaType quotaType, String quotaValue ) throws QuotaException;

    /**
     * Set Quota for container specified with parameters passed containerName - the target container to set quota on,
     * QuotaInfo - about quota information containing quota key and value in preformatted string values
     */
    public void setQuota( ContainerId containerId, Quota quota ) throws QuotaException;

    /**
     * Returns quotas of container
     *
     * @param containerId container ID
     * @return  @see ContainerQuota
     * @throws QuotaException
     */
    ContainerQuota getQuota( ContainerId containerId ) throws QuotaException;


    /**
     * Set Quota for container specified with parameters passed containerName - the target container to set quota on,
     * ConatinerQuota - quota information
     */
    public void setQuota( ContainerId containerId, ContainerQuota quota ) throws QuotaException;

    /**
     * Returns container's quota by quota type
     *
     * @param containerId - container ID
     * @param quotaType - quota type
     *
     * @return - brief description of quota requested
     */
    public Quota getQuota( ContainerId containerId, QuotaType quotaType ) throws QuotaException;

//
//    /**
//     * Return abstract QuotaInfo object
//     *
//     * @param containerId - container ID
//     * @param quotaType - quota type
//     *
//     * @return - brief description of quota requested
//     */
//    Quota getQuota( ContainerId containerId, QuotaType quotaType ) throws QuotaException;

//    /**
//     * Returns RAM quota on container in megabytes
//     *
//     * @param containerId - id of container
//     *
//     * @return - quota in mb
//     */
//    public int getRamQuota( String containerId ) throws QuotaException;
//
//
//    /**
//     * Returns RAM quota object on container
//     *
//     * @param containerId - id of container
//     *
//     * @return - quota object
//     */
//    public RamQuota getRamQuota( String containerId ) throws QuotaException;
//
//
//    /**
//     * Sets RAM quota on container in megabytes
//     *
//     * @param containerId - id of container
//     * @param ramInMb - quota in mb
//     */
//    public void setRamQuota( String containerId, int ramInMb ) throws QuotaException;
//
//
//    /**
//     * Returns CPU quota on container in percent
//     *
//     * @param containerId - id of container
//     *
//     * @return - cpu quota on container in percent
//     */
//    public int getCpuQuota( String containerId ) throws QuotaException;
//
//
//    /**
//     * Returns CPU quota object on container
//     *
//     * @param containerId - id of container
//     *
//     * @return - cpu quota object on container
//     */
//    public CpuQuota getCpuQuotaInfo( String containerId ) throws QuotaException;

//
//    /**
//     * Sets CPU quota on container in percent
//     *
//     * @param containerId - id of container
//     * @param cpuPercent - cpu quota in percent
//     */
//    public void setCpuQuota( String containerId, int cpuPercent ) throws QuotaException;
//
    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param containerId - id of container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( ContainerId containerId ) throws QuotaException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param containerId - name of container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( ContainerId containerId, Set<Integer> cpuSet ) throws QuotaException;

    //    /**
//     * Returns disk partition quota on container
//     *
//     * @param containerId - id of container
//     * @param diskPartition - disk partition
//     *
//     * @return {@code DiskQuota}
//     */
//    public DiskQuota getDiskQuota( String containerId, DiskPartition diskPartition ) throws QuotaException;
//
//
//    /**
//     * Sets disk partition quota on container
//     *
//     * @param containerId - id of container
//     * @param diskQuota - disk quota to set
//     */
//    public void setDiskQuota( String containerId, DiskQuota diskQuota ) throws QuotaException;
//
//    /**
//     * Sets ram quota on container
//     *
//     * @param containerId - id of container
//     * @param ramQuotaInfo - ram quota to set
//     */
//    public void setRamQuota( final String containerId, final RamQuota ramQuotaInfo ) throws QuotaException;
//
//    /**
//     * Returns max available RAM quota in MB on container
//     *
//     * @param containerId - id of container
//     *
//     * @return - max available ram quota in MB
//     */
//    public int getAvailableRamQuota( String containerId ) throws QuotaException;
//
//    /**
//     * Returns max available CPU quota in percent on container
//     *
//     * @param containerId - id of container
//     *
//     * @return - max available cpu quota in percent
//     */
//    public int getAvailableCpuQuota( String containerId ) throws QuotaException;
//
//    /**
//     * Returns max available disk quota on container
//     *
//     * @param containerId - id of container
//     *
//     * @return - max available ram disk quota {@code DiskQuota}
//     */
//    public DiskQuota getAvailableDiskQuota( String containerId, DiskPartition diskPartition ) throws QuotaException;

}
