package org.safehaus.subutai.core.lxc.quota.api;


import org.safehaus.subutai.common.protocol.Container;


/**
 * Created by talas on 10/7/14.
 */
public interface QuotaManager
{
    public void setQuota( Container container, QuotaEnum parameter, String newValue );

    public String getQuota( Container container, QuotaEnum parameter );
}
