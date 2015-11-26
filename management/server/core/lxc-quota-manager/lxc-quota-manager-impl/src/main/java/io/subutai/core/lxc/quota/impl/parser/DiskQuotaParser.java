package io.subutai.core.lxc.quota.impl.parser;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.DiskQuotaUnit;
import io.subutai.common.quota.QuotaParser;


/**
 * Disk info parser
 */
public class DiskQuotaParser implements QuotaParser
{
    private static final String QUOTA_REGEX = "(\\d+(?:[\\.,]\\d+)?)(K|M|G|T|P|E)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );

    private static Map<DiskPartition, DiskQuotaParser> instances = new HashMap<>();
    private DiskPartition diskPartition;


    private DiskQuotaParser( final DiskPartition diskPartition )
    {
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );
        this.diskPartition = diskPartition;
    }


    public static DiskQuotaParser getInstance( DiskPartition diskPartition )
    {
        DiskQuotaParser instance = instances.get( diskPartition );
        if ( instance == null )
        {
            instance = new DiskQuotaParser( diskPartition );
            instances.put( diskPartition, instance );
        }

        return instance;
    }


    private DiskQuotaParser() {}


    public DiskQuota parse( String quotaString )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( quotaString ), "Invalid quota string" );

        if ( quotaString.trim().equalsIgnoreCase( DiskQuotaUnit.UNLIMITED.getAcronym() ) )
        {
            return new DiskQuota( diskPartition, DiskQuotaUnit.UNLIMITED, -1 );
        }

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( quotaString.trim() );
        if ( quotaMatcher.matches() )
        {
            String quotaValue = quotaMatcher.group( 1 );
            double value = Double.parseDouble( quotaValue.replace( ",", "." ) );
            String acronym = quotaMatcher.group( 2 );
            DiskQuotaUnit diskQuotaUnit = DiskQuotaUnit.parseFromAcronym( acronym );
            return new DiskQuota( diskPartition, diskQuotaUnit == null ? DiskQuotaUnit.BYTE : diskQuotaUnit, value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Unparseable result: %s", quotaString ) );
        }
    }
}
