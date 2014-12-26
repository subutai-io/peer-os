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
     * Set Quota for container specified with parameters passed containerName - the target container to set quota on,
     * QuotaInfo - about quota information containing quota key and value in preformatted string values
     */
    public void setQuota( String containerName, QuotaInfo quota ) throws QuotaException;

    /**
     * Get specified quota of container
     */
    public PeerQuotaInfo getQuota( String containerName, QuotaType quotaType ) throws QuotaException;
}
