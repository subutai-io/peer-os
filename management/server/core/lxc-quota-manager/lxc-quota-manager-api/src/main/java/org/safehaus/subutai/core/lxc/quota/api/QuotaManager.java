package org.safehaus.subutai.core.lxc.quota.api;


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
    public void setQuota( String containerHostId, QuotaInfo quota ) throws QuotaException;

    /**
     * Get specified quota of container
     */
    public PeerQuotaInfo getQuota( String containerHostId, QuotaType quotaType ) throws QuotaException;
}
