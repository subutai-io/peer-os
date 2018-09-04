package io.subutai.core.channel.impl.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlException;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.exception.ActionFailedException;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


public class MessageContentUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageContentUtil.class );


    private MessageContentUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static void abortChain( Message message, Throwable ex )
    {
        if ( ex.getClass() == AccessControlException.class )
        {
            abortChain( message, 403, "Access Denied to the resource" );
        }
        else if ( ex.getClass() == LoginException.class )
        {
            abortChain( message, 401, "User is not authorized" );
        }
        else
        {
            abortChain( message, 500, "Internal system Error 500" );
        }

        LOG.error( "****** Error !!! Error in AccessInterceptor:" + ex.toString(), ex );
    }


    //***************************************************************************
    public static void abortChain( Message message, int errorStatus, String errorMessage )
    {
        HttpServletResponse response = ( HttpServletResponse ) message.getExchange().getInMessage()
                                                                      .get( AbstractHTTPDestination.HTTP_RESPONSE );
        try
        {
            response.setStatus( errorStatus );
            response.getOutputStream().write( errorMessage.getBytes(  StandardCharsets.UTF_8 ) );
            response.getOutputStream().flush();

            LOG.error( "****** Error !!! Error in AccessInterceptor:" + " "+ errorMessage
                        +"\n * Blocked URL:" + message.get(Message.REQUEST_URL));

        }
        catch ( Exception e )
        {
            LOG.error( "Error writing to response: " + e.toString(), e );
        }

        message.getInterceptorChain().abort();
    }


    //***************************************************************************
    public static void decryptContent( SecurityManager securityManager, Message message, String hostIdSource,
                                       String hostIdTarget )
    {

        InputStream is = message.getContent( InputStream.class );
        if( is == null )
        {
            LOG.error( "Error decrypting content: No content: " + message.getExchange() );
            return;
        }

        CachedOutputStream os = new CachedOutputStream();

        LOG.debug( String.format( "Decrypting IDs: %s -> %s", hostIdSource, hostIdTarget ) );
        try
        {
            int copied = IOUtils.copyAndCloseInput( is, os );
            os.flush();

            byte[] data = copied > 0 ? decryptData( securityManager, hostIdSource, os.getBytes() ) : null;
            org.apache.commons.io.IOUtils.closeQuietly( os );

            if ( !ArrayUtils.isEmpty( data ) )
            {
                LOG.debug( String.format( "Decrypted payload: \"%s\"", new String( data ) ) );
                message.setContent( InputStream.class, new ByteArrayInputStream( data ) );
            }
            else
            {
                LOG.debug( "Decrypted data is NULL!!!" );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error decrypting content", e );
        }
    }


    //***************************************************************************
    private static byte[] decryptData( SecurityManager securityManager, String hostIdSource, byte[] data )
            throws PGPException
    {

        try
        {
            if ( ArrayUtils.isEmpty( data ) )
            {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            else
            {
                EncryptionTool encTool = securityManager.getEncryptionTool();


                KeyManager keyMan = securityManager.getKeyManager();
                PGPSecretKeyRing secKey = keyMan.getSecretKeyRing( hostIdSource );

                if ( secKey != null )
                {
                    LOG.debug( " ****** Decrypting with: " + hostIdSource + " ****** " );

                    return encTool.decrypt( data, secKey, "" );
                }
                else
                {
                    LOG.debug( String.format( " ****** Decryption error. Could not find Secret key : %s ****** ",
                            hostIdSource ) );
                    throw new PGPException( "Cannot find Secret Key" );
                }
            }
        }
        catch ( Exception ex )
        {
            throw new PGPException( "Error in decryptData", ex );
        }
    }


    public static void encryptContent( SecurityManager securityManager, String hostIdSource, String hostIdTarget,
                                       Message message )
    {
        OutputStream os = message.getContent( OutputStream.class );

        CachedStream cs = new CachedStream();
        message.setContent( OutputStream.class, cs );

        message.getInterceptorChain().doIntercept( message );
        LOG.debug( String.format( "Encrypting IDs: %s -> %s", hostIdSource, hostIdTarget ) );

        try
        {
            cs.flush();
            CachedOutputStream csnew = ( CachedOutputStream ) message.getContent( OutputStream.class );

            byte[] originalMessage = org.apache.commons.io.IOUtils.toByteArray( csnew.getInputStream() );
            LOG.debug( String.format( "Original payload: \"%s\"", new String( originalMessage ) ) );

            csnew.flush();
            org.apache.commons.io.IOUtils.closeQuietly( cs );
            org.apache.commons.io.IOUtils.closeQuietly( csnew );

            //do something with original message to produce finalMessage
            byte[] finalMessage =
                    originalMessage.length > 0 ? encryptData( securityManager, hostIdTarget, originalMessage ) : null;

            if ( !ArrayUtils.isEmpty( finalMessage ) )
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
        catch ( Exception ioe )
        {
            throw new ActionFailedException( "Error encrypting content", ioe );
        }
    }


    private static byte[] encryptData( SecurityManager securityManager, String hostIdTarget, byte[] data )
            throws PGPException
    {
        try
        {
            if ( ArrayUtils.isEmpty( data ) )
            {
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            else
            {
                EncryptionTool encTool = securityManager.getEncryptionTool();
                KeyManager keyMan = securityManager.getKeyManager();
                PGPPublicKey pubKey = keyMan.getRemoteHostPublicKey( hostIdTarget );

                if ( pubKey != null )
                {
                    LOG.debug( String.format( " ****** Encrypting with %s ****** ", hostIdTarget ) );

                    return encTool.encrypt( data, pubKey, true );
                }
                else
                {
                    LOG.debug( String.format( " ****** Encryption error. Could not find Public key : %s ****** ",
                            hostIdTarget ) );
                    throw new PGPException( "Cannot find Public Key" );
                }
            }
        }
        catch ( Exception ex )
        {
            throw new PGPException( "Error in encryptData", ex );
        }
    }
}