package io.subutai.core.lxc.quota.impl.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.NumericValueResource;
import io.subutai.common.resource.ResourceValueParser;


/**
 * RAM value resource parser
 */
public class RamResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+)(KiB|MiB|GiB|TiB|PiB|EiB)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private static RamResourceValueParser instance;


    public static RamResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new RamResourceValueParser();
        }
        return instance;
    }


    private RamResourceValueParser() {}


    @Override
    public ByteValueResource parse( String resource )
    {
        Preconditions.checkNotNull( resource );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            String acronym = quotaMatcher.group( 2 );
            ByteUnit byteUnit = ByteUnit.parseFromAcronym( acronym );

            return new ByteValueResource(
                    ByteValueResource.toBytes( value, byteUnit == null ? ByteUnit.MB : byteUnit ) );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
