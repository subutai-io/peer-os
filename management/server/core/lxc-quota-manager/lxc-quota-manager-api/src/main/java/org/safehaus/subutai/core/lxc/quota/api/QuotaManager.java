package org.safehaus.subutai.core.lxc.quota.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;


/**
 * Created by talas on 10/7/14.
 */
public interface QuotaManager
{
    /**
     * Set Quota for container specified with parameters passed parameter as value to change, it is enum and specific
     * value specified in enum key and newValue can be in any format for setting new value. host is a host with
     * collection of container we intend to modify
     */
    public void setQuota( String containerName, QuotaInfo quota ) throws QuotaException;

    /**
     * Get specified quota of container
     */
    public PeerQuotaInfo getQuota( String containerName, QuotaType quotaType ) throws QuotaException;

    //TODO add simplifed quota management functions and expose them in Peer

    public int getRamQuota( UUID containerId ) throws QuotaException;

    public void setRamQuota( UUID containerId, int ramInMb ) throws QuotaException;

    public int getCpuQuota( UUID containerId ) throws QuotaException;

    public void setCpuQuota( UUID containerId, int cpuPercent ) throws QuotaException;

    public Set<Integer> getCpuSet(UUID containerId) throws QuotaException;

    public void setCpuSet(UUID containerId, Set<Integer> cpuSet) throws QuotaException;
}
