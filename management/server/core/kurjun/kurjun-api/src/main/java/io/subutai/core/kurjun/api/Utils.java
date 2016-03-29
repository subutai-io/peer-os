package io.subutai.core.kurjun.api;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;


public class Utils
{

    public static class MD5
    {
        public static String toString( byte[] md5 )
        {
            return new BigInteger( 1, md5 ).toString( 16 );
        }


        public static byte[] toByteArray( String md5 )
        {
            if ( md5 != null )
            {
                try
                {
                    return Hex.decodeHex( md5.toCharArray() );
                }
                catch ( DecoderException ex )
                {
                    ex.printStackTrace();
                }
            }
            return null;
        }


        public static byte[] streamToByteArray( InputStream inputStream )
        {
            try ( DigestInputStream is = new DigestInputStream( inputStream, DigestUtils.getMd5Digest() ) )
            {
                return is.getMessageDigest().digest();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            return null;
        }


        public static String streamToMD5String( InputStream inputStream )
        {
            try ( DigestInputStream is = new DigestInputStream( inputStream, DigestUtils.getMd5Digest() ) )
            {
                return toString( is.getMessageDigest().digest() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            return null;
        }
    }
}
