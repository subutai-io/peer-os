package org.safehaus.subutai.core.lxc.quota.impl;


import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;


/**
 * Created by talas on 10/7/14.
 */
public class QuotaManagerImpl implements QuotaManager
{
    @Override
    public void setQuota( final Container container, final QuotaEnum parameter, final String newValue )
    {
        //TODO access class for executing linux command
        // TODO then simply send
        //TODO lxc-cgroup -n [container name] parameter.getKey() newValue
        //to take changes effect
    }


    @Override
    public String getQuota( final Container container, final QuotaEnum parameter )
    {
        //TODO the same thing about quotas, execute linux command and return result
        return null;
    }
}
