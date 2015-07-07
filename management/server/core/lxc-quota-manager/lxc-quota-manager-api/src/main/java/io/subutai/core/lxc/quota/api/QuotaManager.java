package io.subutai.core.lxc.quota.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;


/**
 * Quota Manager Api layer for reading/setting quota on a container host. {@code QuotaManager} identifies its {@code
 * ResourceHost} and executes relevant subutai quota command.
 */
public interface QuotaManager
{
    /**
     * Set Quota for container specified with parameters passed containerName - the target container to set quota on,
     * QuotaInfo - about quota information containing quota key and value in preformatted string values
     */
    public void setQuota( String containerName, QuotaInfo quota ) throws QuotaException;


    /**
     * Return abstract QuotaInfo object
     *
     * @param containerId - containerId
     * @param quotaType - quotaType
     *
     * @return - brief description of quota requested
     */
    public QuotaInfo getQuotaInfo( UUID containerId, QuotaType quotaType ) throws QuotaException;


    /**
     * Returns RAM quota on container in megabytes
     *
     * @param containerId - id of container
     *
     * @return - quota in mb
     */
    public int getRamQuota( UUID containerId ) throws QuotaException;


    /**
     * Returns RAM quota object on container
     *
     * @param containerId - id of container
     *
     * @return - quota object
     */
    public RamQuota getRamQuotaInfo( UUID containerId ) throws QuotaException;


    /**
     * Sets RAM quota on container in megabytes
     *
     * @param containerId - id of container
     * @param ramInMb - quota in mb
     */
    public void setRamQuota( UUID containerId, int ramInMb ) throws QuotaException;


    /**
     * Returns CPU quota on container in percent
     *
     * @param containerId - id of container
     *
     * @return - cpu quota on container in percent
     */
    public int getCpuQuota( UUID containerId ) throws QuotaException;


    /**
     * Returns CPU quota object on container
     *
     * @param containerId - id of container
     *
     * @return - cpu quota object on container
     */
    public CpuQuotaInfo getCpuQuotaInfo( UUID containerId ) throws QuotaException;


    /**
     * Sets CPU quota on container in percent
     *
     * @param containerId - id of container
     * @param cpuPercent - cpu quota in percent
     */
    public void setCpuQuota( UUID containerId, int cpuPercent ) throws QuotaException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param containerId - id of container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( UUID containerId ) throws QuotaException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param containerId - id of container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( UUID containerId, Set<Integer> cpuSet ) throws QuotaException;

    /**
     * Returns disk partition quota on container
     *
     * @param containerId - id of container
     * @param diskPartition - disk partition
     *
     * @return {@code DiskQuota}
     */
    public DiskQuota getDiskQuota( UUID containerId, DiskPartition diskPartition ) throws QuotaException;


    /**
     * Sets disk partition quota on container
     *
     * @param containerId - id of container
     * @param diskQuota - disk quota to set
     */
    public void setDiskQuota( UUID containerId, DiskQuota diskQuota ) throws QuotaException;

    /**
     * Sets ram quota on container
     *
     * @param containerId - id of container
     * @param ramQuota - ram quota to set
     */
    public void setRamQuota( final UUID containerId, final RamQuota ramQuota ) throws QuotaException;

    /**
     * Returns max available RAM quota in MB on container
     *
     * @param containerId - id of container
     *
     * @return - max available ram quota in MB
     */
    public int getAvailableRamQuota( UUID containerId ) throws QuotaException;

    /**
     * Returns max available CPU quota in percent on container
     *
     * @param containerId - id of container
     *
     * @return - max available cpu quota in percent
     */
    public int getAvailableCpuQuota( UUID containerId ) throws QuotaException;

    /**
     * Returns max available disk quota on container
     *
     * @param containerId - id of container
     *
     * @return - max available ram disk quota {@code DiskQuota}
     */
    public DiskQuota getAvailableDiskQuota( UUID containerId, DiskPartition diskPartition ) throws QuotaException;
}
