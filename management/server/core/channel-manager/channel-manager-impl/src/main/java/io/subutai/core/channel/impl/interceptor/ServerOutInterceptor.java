package io.subutai.core.channel.impl.interceptor;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.http.HttpResponse;

import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.utils.io.HexUtil;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Out Interceptor
 */
public class ServerOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ServerOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public ServerOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
    }


    /**
     * Intercepts a message. interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.isActive( message, InterceptorState.SERVER_OUT ) )
            {
                LOG.info( "Server OutInterceptor invoked " );

                HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );

                String secured = headers.getHeaderString( Common.SECURED_HEADER_NAME );

                if ( !Strings.isNullOrEmpty( secured ) )
                {
                    String envId = headers.getHeaderString( Common.ENVIRONMENT_ID_HEADER_NAME );
                    String peerId = headers.getHeaderString( Common.PEER_ID_HEADER_NAME );


                    if ( !Strings.isNullOrEmpty( envId ) )
                    {
                        //String outData = getData(message);
                        //String encryptedData = encryptData( envId, "", outData);
                    }
                    else if ( !Strings.isNullOrEmpty( peerId ) )
                    {
                        //String outData = getData(message);
                        //String encryptedData = encryptData( peerId, "", outData );
                    }
                }
                //}
                //***********************************************************************
            }
        }
        catch ( Exception ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }
    }


    /* ******************************************************
     *
     */
    private void encryptData(String hostId, String ip,Message message)
    {
        try
        {
            OutputStream out = message.getContent( OutputStream.class );
            final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream( out );


            CachedOutputStream csnew = (CachedOutputStream) message .getContent(OutputStream.class);
            String currentEnvelopeMessage = IOUtils.toString( csnew.getInputStream(), (String) message.get(Message.ENCODING));


            res = res != null ? res : currentEnvelopeMessage;
            InputStream replaceInStream = IOUtils.tolnputStream(res, (String) message.get(Message.ENCODING));
            IOUtils.copy(replaceInStream, os);
            replaceInStream.close();
            IOUtils.closeQuietly(replaceInStream);
            message.setContent(OutputStream.class, os);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);


            OutputStream out = message.getContent( OutputStream.class );

            if(out == null)
            {
            }
            else
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IOUtils.copy( out, os );
                is.close();

                byte []outData = encryptData( hostId, ip, os.toByteArray() );
                os.close();

//                os.write( outData );
//                os.flush();

                message.setContent(OutputStream.class, os);

            }
        }
        catch ( IOException e )
        {
            LOG.error( " ******** Error encrypting data OutServer data :", e );
        }
    }


    /* ******************************************************
     *
     */
    private byte[] encryptData( String hostId, String ip, byte[] data )
    {
        if(data == null)
            return null;
        else
        {
            EncryptionTool encTool = channelManagerImpl.getSecurityManager().getEncryptionTool();
            KeyManager keyMan = channelManagerImpl.getSecurityManager().getKeyManager();
            PGPPublicKey pubKey = keyMan.getRemoteHostPublicKey( hostId, ip );

            byte[] outData = encTool.encrypt( data, pubKey, false );

            return outData;
        }
    }


    /* ******************************************************
     *
     */
}
