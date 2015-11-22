package io.subutai.core.lxc.quota.impl.parser;


import com.google.common.base.Preconditions;

import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.QuotaParser;


/**
 * CPU info parser
 */
public class CpuQuotaParser implements QuotaParser
{
    private static CpuQuotaParser instance;


    public static CpuQuotaParser getInstance()
    {
        if ( instance == null )
        {
            instance = new CpuQuotaParser();
        }
        return instance;
    }


    private CpuQuotaParser() {}


    @Override
    public CpuQuota parse( final String quota )
    {
        Preconditions.checkNotNull( quota );
        int cpuAmount = Integer.parseInt( quota.trim() );
        return new CpuQuota( cpuAmount );
    }
}
