package io.subutai.common.security.crypto.pgp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


/********************************************
 * Key utility class for PGP Keys
 */
public class PGPKeyUtil
{
    public static final BouncyCastleProvider provider = new BouncyCastleProvider();

    public static final int HEX_SHORT_KEY_ID_LENGTH = 8;
    public static final int HEX_LONG_KEY_ID_LENGTH = 16;
    public static final int HEX_V3_FINGERPRINT_LENGTH = 32;
    public static final int HEX_V4_FINGERPRINT_LENGTH = 40;

    private static final Logger LOGGER = LoggerFactory.getLogger( PGPKeyUtil.class );


    /**************************************************************************************
     *
     */
    static
    {
        Security.addProvider( provider );
    }


    /**************************************************************************************
    *
    */
    private PGPKeyUtil()
    {
    }


    /**************************************************************************************
     * Hex encode given numeric key id.
     *
     * @param keyId key id
     * @return hex encoded long key id string representing given numeric key id
     */
    public static String encodeNumericKeyId( long keyId )
    {
        // format long value to hex format without leading 0x and padding 0 to make up 16 digits
        return String.format( "%016X", keyId );
    }


    /**************************************************************************************
     * Hex encode given numeric key id as a short key id.
     *
     * @param keyId key id
     * @return hex encoded short key id string representing given numeric key id
     */
    public static String encodeNumericKeyIdShort( long keyId )
    {
        String longKeyId = encodeNumericKeyId( keyId );
        return longKeyId.substring( 8 );
    }


    /**************************************************************************************
     * Retrieves long key id out of given fingerprint. Fingerprint is a 40 hex digits, last 16 digits make up the long
     * key id.
     *
     * @param fingerprint hex encoded fingerprint
     * @return long key id
     */
    public static String getKeyId( String fingerprint )
    {
        return fingerprint.substring( 24 ).toUpperCase();
    }


    /**************************************************************************************
     * Retrieves long key id out of given fingerprint. Fingerprint is a 40 hex digits, last 16 digits make up the long
     * key id.
     *
     * @param fingerprint fingerprint bytes array
     * @return long key id
     */
    public static String getKeyId( byte[] fingerprint )
    {
        char[] hex = Hex.encodeHex( fingerprint, false );
        return getKeyId( new String( hex ) );
    }

    /**************************************************************************************
     * Retrieves long key id out of given fingerprint. Fingerprint is a 40 hex digits, last 16 digits make up the long
     * key id.
     *
     * @param fingerprint fingerprint bytes array
     * @return long key id
     */
    public static String getFingerprint( byte[] fingerprint )
    {
        char[] hex = Hex.encodeHex( fingerprint, false );
        String fingerprintStr = new String( hex );

        return fingerprintStr;
    }


    /**************************************************************************************
     * Retrieves short key id out of given fingerprint. Fingerprint a is 40 hex digits, last 8 digits make up the short
     * key id.
     *
     * @param fingerprint hex encoded fingerprint
     * @return short key id
     */
    public static String getShortKeyId( String fingerprint )
    {
        return fingerprint.substring( 32 ).toUpperCase();
    }


    /**************************************************************************************
     * Retrieves short key id out of given fingerprint. Fingerprint a is 40 hex digits, last 8 digits make up the short
     * key id.
     *
     * @param fingerprint fingerprint bytes array
     * @return short key id
     */
    public static String getShortKeyId( byte[] fingerprint )
    {
        char[] hex = Hex.encodeHex( fingerprint, false );
        return getShortKeyId( new String( hex ) );
    }


    /**************************************************************************************
     * Checks if given string represents a valid hex encoded V4 fingerprint.
     *
     * @param keyId fingerprint value to check
     * @return {@code true} if given value is a valid v4 fingerprint; {@code false} otherwise
     */
    public static boolean isFingerprint( String keyId )
    {
        return isValidKeyId( keyId ) && keyId.length() == HEX_V4_FINGERPRINT_LENGTH;
    }


    /**************************************************************************************
     * Checks if given string represents a valid hex encoded 64-bit long key id.
     *
     * @param keyId value to check
     * @return {@code true} if given value is a valid long key id; {@code false} otherwise
     */
    public static boolean isLongKeyId( String keyId )
    {
        return isValidKeyId( keyId ) && keyId.length() == HEX_LONG_KEY_ID_LENGTH;
    }

    
    /******************************************************************************************************************************** 
     * Checks if given string represents a valid hex encoded 32-bit short key id.
     *
     * @param keyId value to check
     * @return {@code true} if given value is a valid short key id; {@code false} otherwise
     */
    public static boolean isShortKeyId( String keyId )
    {
        return isValidKeyId( keyId ) && keyId.length() == HEX_SHORT_KEY_ID_LENGTH;
    }


    /**************************************************************************************
     * Checks if given string represents a valid key id whether it is a short key id, long key id, or a fingerprint.
     *
     * @param keyId value to check
     * @return {@code true} if given value is a valid key id; {@code false} otherwise
     */
    public static boolean isValidKeyId( String keyId )
    {
        if ( keyId == null )
        {
            return false;
        }
        try
        {
            // check if digits are valid hex symbols
            Hex.decodeHex( keyId.toCharArray() );
            switch ( keyId.length() )
            {
                //  8 digits make up  32 bit short key id
                // 16 digits make up  64-bit long key id
                // 40 digits make up 160-bit version 4 fingerprint
                case HEX_SHORT_KEY_ID_LENGTH:
                case HEX_LONG_KEY_ID_LENGTH:
                case HEX_V4_FINGERPRINT_LENGTH:
                    return true;
                case HEX_V3_FINGERPRINT_LENGTH:
                default:
            }
        }
        catch ( DecoderException ex )
        {
            LOGGER.info( "Invalid hex formatted key id", ex );
        }
        return false;
    }


    /**************************************************************************************
     * Exports given public key as ASCII armored text.
     *
     * @param pgpKey key to export
     * @return ASCII armored key text
     * @throws PGPException
     */
    public static String exportAscii( PGPPublicKey pgpKey ) throws PGPException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(  );
        try ( OutputStream os = new ArmoredOutputStream( out ) )
        {
            pgpKey.encode( os );
        }
        catch ( IOException ex )
        {
            throw new PGPException( "Failed to export PGP key", ex );
        }
        return out.toString();
    }


    /********************************************
     *
     *
     * @param key
     * @return
     * @throws PGPException
     */
    public static PGPPublicKey readPublicKey( String key ) throws PGPException
    {
        return readPublicKey( new ByteArrayInputStream( key.getBytes( StandardCharsets.UTF_8 ) ) );
    }


    /********************************************
     *
     *
     * @param keyMaterial
     * @return
     * @throws PGPException
     */
    public static PGPPublicKey readPublicKey( byte[] keyMaterial ) throws PGPException
    {
        return readPublicKey( new ByteArrayInputStream( keyMaterial ) );
    }


    /********************************************
     * A simple routine that opens a key ring file and loads the first available key suitable for encryption.
     *
     * @param instr data stream containing the public key data
     *
     * @return the first public key found.
     *
     * @throws PGPException
     */
    public static PGPPublicKey readPublicKey( InputStream instr ) throws PGPException
    {
        PGPPublicKeyRingCollection pgpPub;
        try
        {
            instr = org.bouncycastle.openpgp.PGPUtil.getDecoderStream( instr );
            pgpPub = new PGPPublicKeyRingCollection( instr,new JcaKeyFingerprintCalculator() );
        }
        catch ( IOException | PGPException ex )
        {
            throw new PGPException( "Failed to init public key ring", ex );
        }

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //

        Iterator keyRingIter = pgpPub.getKeyRings();
        while ( keyRingIter.hasNext() )
        {
            PGPPublicKeyRing keyRing = ( PGPPublicKeyRing ) keyRingIter.next();

            Iterator keyIter = keyRing.getPublicKeys();
            while ( keyIter.hasNext() )
            {
                PGPPublicKey key = ( PGPPublicKey ) keyIter.next();

                if ( key.isEncryptionKey() )
                {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException( "Can't find encryption key in key ring." );
    }


    /* *******************************************
     *
     */
    public static PGPPublicKeyRing readPublicKeyRing( InputStream instr ) throws PGPException
    {
        PGPPublicKeyRingCollection pgpPub;
        try
        {
            instr = org.bouncycastle.openpgp.PGPUtil.getDecoderStream( instr );
            pgpPub = new PGPPublicKeyRingCollection( instr,new JcaKeyFingerprintCalculator() );
        }
        catch ( IOException | PGPException ex )
        {
            throw new PGPException( "Failed to init public key ring", ex );
        }

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //

        Iterator keyRingIter = pgpPub.getKeyRings();
        while ( keyRingIter.hasNext() )
        {
            PGPPublicKeyRing keyRing = ( PGPPublicKeyRing ) keyRingIter.next();

            return keyRing;

        }

        throw new IllegalArgumentException( "Can't find encryption key in key ring." );
    }


    /* *******************************************
     *
     */
    public static PGPPublicKey readPublicKey( PGPPublicKeyRing keyRing ) throws PGPException
    {
        try
        {
            Iterator keyIter = keyRing.getPublicKeys();

            while ( keyIter.hasNext() )
            {
                PGPPublicKey key = ( PGPPublicKey ) keyIter.next();

                if ( key.isEncryptionKey() )
                {
                    return key;
                }
            }
        }
        catch(Exception ex)
        {
            return null;
        }

        return null;
    }

}


