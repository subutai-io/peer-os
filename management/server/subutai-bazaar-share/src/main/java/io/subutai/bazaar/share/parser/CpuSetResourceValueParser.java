package io.subutai.bazaar.share.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import io.subutai.bazaar.share.resource.ResourceValueParser;
import io.subutai.bazaar.share.resource.StringValueResource;


/**
 * Cpu set value resource parser
 */
public class CpuSetResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "((\\d+-\\d+)|(0))";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private static CpuSetResourceValueParser instance;


    public static CpuSetResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new CpuSetResourceValueParser();
        }
        return instance;
    }


    private CpuSetResourceValueParser() {}


    @Override
    public StringValueResource parse( String resource )
    {
        Preconditions.checkNotNull( resource );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            return new StringValueResource( value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
