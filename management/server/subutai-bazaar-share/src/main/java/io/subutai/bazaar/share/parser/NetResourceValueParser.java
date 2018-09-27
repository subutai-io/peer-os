package io.subutai.bazaar.share.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import io.subutai.bazaar.share.resource.NumericValueResource;
import io.subutai.bazaar.share.resource.ResourceValueParser;


/**
 * Network value resource parser
 */
public class NetResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+)(\\.\\d+)?(Kbps)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private static NetResourceValueParser instance;


    public static NetResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new NetResourceValueParser();
        }
        return instance;
    }


    private NetResourceValueParser() {}


    @Override
    public NumericValueResource parse( String resource )
    {
        Preconditions.checkNotNull( resource );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );

        if ( quotaMatcher.matches() )
        {
            String intPart = quotaMatcher.group( 1 );
            String decPart = quotaMatcher.group( 2 );
            String acronym = quotaMatcher.group( 3 );

            if ( acronym != null )
            {
                if ( !"Kbps".equals( acronym ) )
                {
                    throw new IllegalArgumentException(
                            String.format( "Illegal acronym of network resource: %s", resource ) );
                }
            }
            return new NumericValueResource( intPart + ( decPart != null ? decPart : "" ) );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse network resource: %s", resource ) );
        }
    }
}
