package org.safehaus.subutai.common.util;


import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;


/**
 * Provides utility functions for working with Strings
 */
public class StringUtil
{


    private StringUtil()
    {
    }


    public static String trimToSize( String str, int size )
    {
        if ( !isStringNullOrEmpty( str ) )
        {
            if ( str.length() > size )
            {
                return str.substring( 0, size );
            }
        }

        return str;
    }


    public static List<String> splitString( String str, String delimiter )
    {
        List<String> result = Lists.newArrayList();
        StringTokenizer t = new StringTokenizer( str, delimiter );
        while ( t.hasMoreTokens() )
        {
            result.add( t.nextToken().trim() );
        }

        return result;
    }


    public static int countNumberOfOccurrences( String strToSearch, String strToCount )
    {
        int count = 0;
        if ( !isStringNullOrEmpty( strToSearch ) && !isStringNullOrEmpty( strToCount ) )
        {
            int idx = strToSearch.indexOf( strToCount );
            while ( idx > -1 )
            {
                count++;
                idx = strToSearch.indexOf( strToCount, idx + 1 );
            }
        }
        return count;
    }


    public static boolean isStringNullOrEmpty( String str )
    {
        return str == null || str.trim().isEmpty();
    }


    public static boolean isNumeric( String str )
    {
        try
        {
            Double.parseDouble( str );
            return true;
        }
        catch ( NullPointerException | NumberFormatException e )
        {
            return false;
        }
    }


    public static boolean areStringsEqual( String str1, String str2 )
    {
        return str1 != null && str2 != null && str2.equals( str1 );
    }


    public static String joinStrings( Set<String> strings, char delimiter, boolean wrapWithQuotes )
    {
        StringBuilder sb = new StringBuilder();
        for ( String string : strings )
        {
            if ( wrapWithQuotes )
            {
                sb.append( "\"" );
            }
            sb.append( string );
            if ( wrapWithQuotes )
            {
                sb.append( "\"" );
            }
            sb.append( delimiter );
        }
        sb.replace( sb.length() - 1, sb.length(), "" );
        return sb.toString();
    }


    public static int getLen( String str )
    {
        if ( !isStringNullOrEmpty( str ) )
        {
            return str.length();
        }
        return 0;
    }
}
