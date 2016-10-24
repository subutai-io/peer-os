package io.subutai.core.identity.impl.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Security Utils for Hashing and etc
 */
public class SecurityUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityUtil.class.getName() );


    private static String ByteArrayToString( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder();
        for ( final byte aByte : bytes )
        {
            sb.append( Integer.toString( ( aByte & 0xff ) + 0x100, 16 ).substring( 1 ) );
        }

        return sb.toString();
    }


    /* *************************************************
     */
    public static String generateHash( String item )
    {
        String generatedPassword = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( item.getBytes() );
            byte[] bytes = md.digest();
            generatedPassword = ByteArrayToString( bytes );
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.warn( e.getMessage() );
        }

        return generatedPassword;
    }


    /* *************************************************
     */
    public static String generateSecurePassword( String passwordToHash, String salt )
    {
        String generatedPassword = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( salt.getBytes() );
            byte[] bytes = md.digest( passwordToHash.getBytes() );

            generatedPassword = ByteArrayToString( bytes );
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.warn( e.getMessage() );
        }
        return generatedPassword;
    }


    /* *************************************************
     */
    public static String generateSecureRandom() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        SecureRandom sr = SecureRandom.getInstance( "SHA1PRNG", "SUN" );
        byte[] salt = new byte[16];
        sr.nextBytes( salt );

        return Arrays.toString( salt );
    }
}
