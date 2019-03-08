package io.subutai.common.util;


import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import com.google.common.base.Strings;

import io.subutai.common.security.SshEncryptionType;


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
        if ( StringUtils.isBlank( str ) )
        {
            return "";
        }
        else
        {
            return StringEscapeUtils.escapeHtml4( str );
        }
    }


    public static boolean isBase64( String str )
    {
        return Base64.isBase64( str );
    }


    public static boolean isValidSshPublicKey( String sshKey )
    {

        if ( !StringUtils.isBlank( sshKey ) )
        {
            String[] parts = sshKey.trim().split( "\\s+" );

            if ( parts.length == 2 || parts.length == 3 )
            {
                SshEncryptionType type = SshEncryptionType.parseTypeFromKey( parts[0] );

                if ( type != SshEncryptionType.UNKNOWN )
                {
                    return isBase64( parts[1] );
                }
            }
        }

        return false;
    }


    public static String unEscapeHtml( String str )
    {
        if ( StringUtils.isBlank( str ) )
        {
            return "";
        }
        else
        {
            return StringEscapeUtils.unescapeHtml4( str );
        }
    }


    /**
     * ***********************************************************************************
     * Checks if Input contains any HTML or CSS tags
     *
     * @param str input String
     *
     * @return Validated String
     */
    public static boolean containsHtml( String str )
    {
        if ( StringUtils.isBlank( str ) )
        {
            return false;
        }
        else
        {
            return Jsoup.isValid( str, Whitelist.none() ) ? false : true;
        }
    }


    /**
     * ***********************************************************************************
     * Removes Special HTML Tags
     *
     * @param str input String
     *
     * @return Validated String
     */
    public static String removeHtml( String str )
    {
        if ( StringUtils.isBlank( str ) )
        {
            return "";
        }
        else
        {
            return Jsoup.parse( str ).text();
        }
    }


    /**
     * ***********************************************************************************
     * Removes Special HTML Tags and Non-AlfaNumeric Chars
     *
     * @param str input String
     * @param removeSpaces if TRUE removes whitespaces
     *
     * @return Validated String
     */
    public static String removeHtmlAndSpecialChars( String str, boolean removeSpaces )
    {
        String noHTML = removeHtml( str );

        return removeSpecialChars( noHTML, removeSpaces );
    }


    /**
     * ***********************************************************************************
     * Removes Special Non-AlfaNumeric Chars
     *
     * @param str input String
     * @param removeSpaces if TRUE removes whitespaces
     *
     * @return Validated String
     */
    public static String removeSpecialChars( String str, boolean removeSpaces )
    {
        if ( StringUtils.isBlank( str ) )
        {
            return "";
        }
        else
        {
            if ( removeSpaces )
            {
                return str.replaceAll( "[^a-zA-Z0-9._-]", "" ).trim();
            }
            else
            {
                return str.replaceAll( "[^a-zA-Z0-9\\s._-]", "" ).trim();
            }
        }
    }


    /**
     * ***********************************************************************************
     * Removes Special HTML Tags and Non-AlfaNumeric Chars
     *
     * @param email Email to be validated
     *
     * @return Is valid mail
     */
    public static boolean isValidEmail( String email )
    {
        EmailValidator emailvalidator = EmailValidator.getInstance();

        return emailvalidator.isValid( email );
    }
}
