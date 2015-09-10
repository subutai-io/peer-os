package io.subutai.core.channel.impl.util;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class MessageContentUtil
{
    public static int checkUrlAccessibility( final int currentStatus, final URL url, final String basePath)
    {
        int status = currentStatus;
        if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X1 ) )
        {
            if ( ChannelSettings.checkURLArray( basePath, ChannelSettings.URL_ACCESS_PX1 ) == 0 )
            {
                status = 1;
            }
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
        {
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X3 ) )
        {
            //----------------------------------------------------------------------
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SPECIAL_PORT_X1 ) || url.getPort() == Integer
                .parseInt( ChannelSettings.SPECIAL_SECURE_PORT_X1 ) )
        {
        }
        else
        {
            status = 0;
        }
        return status;
    }


    /* ******************************************************
     *
     */
    public static void decryptMessageContent(SecurityManager securityManager, Message message, String hostId )
    {

        InputStream is = message.getContent( InputStream.class );
        CachedOutputStream os = new CachedOutputStream();

        try
        {
            IOUtils.copyAndCloseInput( is, os );
            os.flush();

            byte[] data = decryptData(securityManager, hostId, os.getBytes() );
            org.apache.commons.io.IOUtils.closeQuietly( os );

            if(data!=null)
            {
                message.setContent( InputStream.class, new ByteArrayInputStream( data ));
            }

        }
        catch ( IOException e )
        {

        }
    }


    /* ******************************************************
     *
     */
    private static byte[] decryptData(SecurityManager securityManager, String hostId, byte[] data )
    {

        try
        {
            if ( data == null || data.length == 0)
            {
                return null;
            }
            else
            {
                EncryptionTool encTool = securityManager.getEncryptionTool();
                KeyManager keyMan = securityManager.getKeyManager();
                PGPSecretKeyRing secKey = keyMan.getSecretKeyRing(  hostId );

                byte[] outData = encTool.decrypt( data, secKey, "" );

                return outData;
            }
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    /* ******************************************************
    *
    */
    public static void encryptMessageContent(SecurityManager securityManager, String hostId, String ip, Message message )
    {
        OutputStream os = message.getContent( OutputStream.class );

        CachedStream cs = new CachedStream();
        message.setContent( OutputStream.class, cs );

        message.getInterceptorChain().doIntercept( message );

        try
        {
            cs.flush();
            org.apache.commons.io.IOUtils.closeQuietly( cs );
            CachedOutputStream csnew = ( CachedOutputStream ) message.getContent( OutputStream.class );

            byte[] originalMessage = org.apache.commons.io.IOUtils.toByteArray( csnew.getInputStream() );
            csnew.flush();
            org.apache.commons.io.IOUtils.closeQuietly( csnew );

            //do something with original message to produce finalMessage
            byte[] finalMessage = encryptData(securityManager, hostId, ip, originalMessage );

            if ( finalMessage != null )
            {
                InputStream replaceInStream = new ByteArrayInputStream( finalMessage );

                org.apache.commons.io.IOUtils.copy( replaceInStream, os );
                replaceInStream.close();
                org.apache.commons.io.IOUtils.closeQuietly( replaceInStream );

                os.flush();
                message.setContent( OutputStream.class, os );
            }

            org.apache.commons.io.IOUtils.closeQuietly( os );
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
    }


    /* ******************************************************
     *
     */
    private static  byte[] encryptData(SecurityManager securityManager, String hostId, String ip, byte[] data )
    {
        if ( data == null || data.length == 0 )
        {
            return null;
        }
        else
        {
            EncryptionTool encTool = securityManager.getEncryptionTool();
            KeyManager keyMan = securityManager.getKeyManager();
            PGPPublicKey pubKey = keyMan.getRemoteHostPublicKey( hostId, ip );

            byte[] outData = encTool.encrypt( data, pubKey, false );

            return outData;
        }
    }
}
