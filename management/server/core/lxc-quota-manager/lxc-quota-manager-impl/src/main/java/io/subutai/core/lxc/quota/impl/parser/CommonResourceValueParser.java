package io.subutai.core.lxc.quota.impl.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.resource.ResourceValueParser;


/**
 * Common resource value parser
 */
public class CommonResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+(?:[\\.,]\\d+)?)(K|M|G|T|P|E)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );

    private static CommonResourceValueParser instance;

    public static CommonResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new CommonResourceValueParser();
        }
        return instance;
    }


    private CommonResourceValueParser() {}


    public ResourceValue parse( String resource )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resource ), "Invalid resource string" );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            String acronym = quotaMatcher.group( 2 );
            MeasureUnit measureUnit = MeasureUnit.parseFromAcronym( acronym );
            return new ResourceValue( value, measureUnit == null ? MeasureUnit.BYTE : measureUnit );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
