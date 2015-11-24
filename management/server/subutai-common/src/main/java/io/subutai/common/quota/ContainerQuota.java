package io.subutai.common.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * Container quota class
 */
public class ContainerQuota
{
    private Map<QuotaType, Quota> quotas = new HashMap<>();


    public void addQuota( Quota quotaInfo )
    {
        Preconditions.checkNotNull( quotaInfo );
        Preconditions.checkNotNull( quotaInfo.getType() );
        Preconditions.checkNotNull( quotaInfo.getKey() );
        Preconditions.checkNotNull( quotaInfo.getValue() );

        quotas.put( quotaInfo.getType(), quotaInfo );
    }


    public Quota getQuota( QuotaType quotaType )
    {
        return quotas.get( quotaType );
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ContainerQuota{" );
        sb.append( "quotas=" ).append( quotas );
        sb.append( '}' );
        return sb.toString();
    }


    public Collection<Quota> getAllQuotaInfo()
    {
        return quotas.values();
    }
}
