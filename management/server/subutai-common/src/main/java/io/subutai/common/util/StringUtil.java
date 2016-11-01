package io.subutai.common.util;


import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.validator.routines.EmailValidator;

import com.google.common.base.Strings;


/**
 * String utilities.
 * Google and Apache libraries are used for escape methods.
 * Recommended for escaping is Apache.Commons
 */
public class StringUtil
{

    private StringUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static String escapeHtml( String str )
    {
        if ( Strings.isNullOrEmpty( str ) )
        {
            return "";
        }
        else
        {
            return StringEscapeUtils.escapeHtml4( str );
        }
    }


    public static String unEscapeHtml( String str )
    {
        if ( Strings.isNullOrEmpty( str ) )
        {
            return "";
        }
        else
        {
            return StringEscapeUtils.unescapeHtml4( str );
        }
    }


    public static boolean containsHtml( String str )
    {
        if ( Strings.isNullOrEmpty( str ) )
        {
            return false;
        }
        else
        {
            return Jsoup.isValid( str, Whitelist.none() ) ? false : true;
        }
    }


    public static String removeHtml( String str )
    {
        if ( Strings.isNullOrEmpty( str ) )
        {
            return "";
        }
        else
        {
            return Jsoup.parse( str ).text();
        }
    }


    public static String removeHtmlAndSpecialChars( String str )
    {
        String noHTML = removeHtml( str );

        return removeSpecialChars( noHTML );
    }


    public static String removeSpecialChars( String str )
    {
        if ( Strings.isNullOrEmpty( str ) )
        {
            return "";
        }
        else
        {
            return str.replaceAll( "[^a-zA-Z0-9._-]", "" );
        }
    }


    public static boolean isValidEmail( String email )
    {
        EmailValidator emailvalidator = EmailValidator.getInstance();

        return emailvalidator.isValid( email );
    }
}
