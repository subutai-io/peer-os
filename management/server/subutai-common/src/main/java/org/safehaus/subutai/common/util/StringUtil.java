package org.safehaus.subutai.common.util;


import java.util.Set;


/**
 * Provides utility functions for working with Strings
 */
public class StringUtil
{


    private StringUtil()
    {
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
