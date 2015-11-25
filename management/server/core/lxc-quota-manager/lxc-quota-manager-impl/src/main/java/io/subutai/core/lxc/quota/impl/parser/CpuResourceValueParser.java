package io.subutai.core.lxc.quota.impl.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.resource.ResourceValueParser;


/**
 * CPU resource parser
 */
public class CpuResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+)(%)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private static CpuResourceValueParser instance;


    public static CpuResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new CpuResourceValueParser();
        }
        return instance;
    }


    private CpuResourceValueParser() {}


    @Override
    public ResourceValue parse( final String resource )
    {
        Preconditions.checkNotNull( resource );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            String acronym = quotaMatcher.group( 2 );
            MeasureUnit measureUnit = MeasureUnit.parseFromAcronym( acronym );
            if ( measureUnit != null && measureUnit != MeasureUnit.PERCENT )
            {
                throw new IllegalArgumentException( "Invalid measure unit." );
            }
            return new ResourceValue( value, measureUnit == null ? MeasureUnit.PERCENT : measureUnit );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
