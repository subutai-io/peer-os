package io.subutai.common.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.RegistrationData;


/**
 * Security utilities
 */
public class SecurityUtilities
{
    private static Logger LOG = LoggerFactory.getLogger( SecurityUtilities.class );
    public static final int DEFAULT_KEY_SIZE = 128;


    /**
     * Generates a new AES-Key.
     *
     * @return the AES-Key
     */
    public static byte[] generateKey()
    {
        try
        {
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance( "AES" );
            kgen.init( DEFAULT_KEY_SIZE ); // 192 and 256 bits may not be available
            //kgen.init(256); // needs unlimited jurisdiction files

            // Generate the secret key specs.
            SecretKey skey = kgen.generateKey();
            byte[] key = skey.getEncoded();

            return key;
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.warn( e.getMessage() );
        }
        return null;
    }


    public static byte[] generateKey( byte[] data )
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
            byte[] key = sha.digest( data );
            return Arrays.copyOf( key, DEFAULT_KEY_SIZE / 8 );
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.warn( e.getMessage() );
        }
        return null;
    }


    /**
     * Encrypts or decrypts the given data with the given Secret-Key.
     *
     * @param data the data
     * @param key the Secret-Key
     * @param mode which operation to perform (Cipher.DECRYPT_MODE or Cipher.ENCRYPT_MODE)
     *
     * @return the encrypted or decrypted data
     *
     * @throws Exception if the en/decryption failed
     */
    public static byte[] cipher( byte[] data, byte[] key, int mode ) throws Exception
    {
        // generate secret key specs, provider independent
        SecretKeySpec keySpec = new SecretKeySpec( key, "AES" );

        // instantiate cipher
        Cipher cipher = Cipher.getInstance( "AES" );

        // encrypt/decrypt
        cipher.init( mode, keySpec );
        byte[] encrypted = cipher.doFinal( data );

        return encrypted;
    }
}
