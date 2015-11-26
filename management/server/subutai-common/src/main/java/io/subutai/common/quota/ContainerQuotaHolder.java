package io.subutai.common.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.ResourceType;


/**
 * Container quota holder class
 */
public class ContainerQuotaHolder
{
    private Map<ResourceType, CommonQuota> quotas = new HashMap<>();


    public void addQuota( CommonQuota quota )
    {
        Preconditions.checkNotNull( quota );
        Preconditions.checkNotNull( quota.getType() );
        Preconditions.checkNotNull( quota.getValue() );

        quotas.put( quota.getType(), quota );
    }


    public CommonQuota getQuota( ResourceType resourceType )
    {
        return quotas.get( resourceType );
    }


    public Collection<CommonQuota> getAllQuotas()
    {
        return quotas.values();
    }
}
