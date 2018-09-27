package io.subutai.bazaar.share.parser;


import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ResourceValueParser;


/**
 * Byte value resource parser
 */
public class DiskResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+(?:[\\.,]\\d+)?)(KiB|MiB|GiB|TiB|PiB|EiB)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );

    private static DiskResourceValueParser instance;


    private DiskResourceValueParser()
    {
    }


    public static DiskResourceValueParser getInstance()
    {

        if ( instance == null )
        {
            instance = new DiskResourceValueParser();
        }
        return instance;
    }


    @Override
    public ByteValueResource parse( String resource )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resource ), "Invalid resource string" );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            String acronym = quotaMatcher.group( 2 );
            ByteUnit byteUnit = ByteUnit.parseFromAcronym( acronym );

            return new ByteValueResource(
                    ByteValueResource.toBytes( new BigDecimal( value ), byteUnit == null ? ByteUnit.GB : byteUnit ) );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
