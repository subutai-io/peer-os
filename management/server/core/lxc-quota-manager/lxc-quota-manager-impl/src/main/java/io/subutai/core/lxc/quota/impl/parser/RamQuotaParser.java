package io.subutai.core.lxc.quota.impl.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.quota.QuotaParser;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.quota.RamQuotaUnit;


/**
 * RAM info parser
 */
public class RamQuotaParser implements QuotaParser
{
    private static final String QUOTA_REGEX = "(\\d+)(K|M|G)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );

    private static RamQuotaParser instance;


    public static RamQuotaParser getInstance()
    {
        if ( instance == null )
        {
            instance = new RamQuotaParser();
        }
        return instance;
    }


    private RamQuotaParser() {}


    public RamQuota parse( String qota )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( qota ), "Invalid quota string" );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( qota.trim() );
        if ( quotaMatcher.matches() )
        {
            String quotaValue = quotaMatcher.group( 1 );
            int value = Integer.parseInt( quotaValue );
            String acronym = quotaMatcher.group( 2 );
            RamQuotaUnit ramQuotaUnit = RamQuotaUnit.parseFromAcronym( acronym );
            return new RamQuota( ramQuotaUnit == null ? RamQuotaUnit.BYTE : ramQuotaUnit, value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Unparseable result: %s", qota ) );
        }
    }
}
