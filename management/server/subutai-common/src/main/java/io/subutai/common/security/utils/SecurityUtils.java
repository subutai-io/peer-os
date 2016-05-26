package io.subutai.common.security.utils;


import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * Security Utils for Hashing and etc
 */
public class SecurityUtils
{

    private static String ByteArrayToString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


    /* *************************************************
     */
    public static int generateShortRandom()
    {
        Random randomGenerator = new Random();

        return randomGenerator.nextInt(99000 - 10000 + 1) + 10000;
    }


    /* *************************************************
     */
    public static long generateLongRandom()
    {
        Random randomGenerator = new Random();
        return randomGenerator.nextLong();
    }


    /* *************************************************
     */
    public static String generateUUIDRandom()
    {
        String uuid = UUID.randomUUID().toString();

        uuid += "-" + generateShortRandom();

        return uuid;
    }


    /* *************************************************
     */
    public static String generateHash(String item) throws NoSuchAlgorithmException
    {
        String generatedPassword = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(item.getBytes());
            byte[] bytes = md.digest();
            generatedPassword = ByteArrayToString(bytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new NoSuchAlgorithmException(e);
        }

        return generatedPassword;
    }


    /* *************************************************
     */
    public static String generateSecurePassword( String passwordToHash, String salt ) throws NoSuchAlgorithmException
    {
        String generatedPassword = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( salt.getBytes() );
            byte[] bytes = md.digest( passwordToHash.getBytes() );

            generatedPassword = ByteArrayToString(bytes);
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new NoSuchAlgorithmException(e);
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

        return salt.toString();
    }


    /**
     * Calculates the md5 checksum of the given input stream
     *
     * @param is
     * @return md5 checksum, or <code>null</code> if exception occurred
     */
    public static byte[] calculateMd5( InputStream is ) throws IOException
    {
        byte[] md5 = null;
        try
        {
            md5 = DigestUtils.md5( is );
        }
        catch ( IOException e )
        {
            throw new IOException(e);
        }
        return md5;
    }


}
