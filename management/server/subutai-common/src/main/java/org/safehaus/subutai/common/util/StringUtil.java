package org.safehaus.subutai.common.util;


/**
 * Provides utility functions for working with Strings
 */
public class StringUtil
{

    public static int countNumberOfOccurences( String strToSearch, String strToCount )
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
        catch ( NumberFormatException e )
        {
        }

        return false;
    }


    public static boolean areStringsEqual( String str1, String str2 )
    {
        if ( str1 == null )
        {
            return false;
        }
        if ( str2 == null )
        {
            return false;
        }

        return str2.equals( str1 );
    }
}
