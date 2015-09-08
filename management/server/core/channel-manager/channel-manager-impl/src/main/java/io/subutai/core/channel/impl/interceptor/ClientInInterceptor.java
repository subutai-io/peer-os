package io.subutai.core.channel.impl.interceptor;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Strings;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 *
 */
public class ClientInInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ClientInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;

    //******************************************************************
    public ClientInInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.RECEIVE);
        this.channelManagerImpl = channelManagerImpl;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.CLIENT_IN.isActive( message ) )
            {
                LOG.info( " ****** Client InInterceptor invoked ******** " );

                URL url = new URL( ( String ) message.getExchange().getOutMessage().get( Message.ENDPOINT_ADDRESS ) );

                if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    HttpHeaders headers = new HttpHeadersImpl( message);

                    String spHeader = headers.getHeaderString( Common.SPECIAL_HEADER_NAME );

                    if(!Strings.isNullOrEmpty( spHeader ))
                    {
                        String envId   = headers.getHeaderString( Common.ENVIRONMENT_ID_HEADER_NAME );
                        String peerId  = headers.getHeaderString( Common.PEER_ID_HEADER_NAME );

                        if ( !Strings.isNullOrEmpty( envId ) )
                        {
                            decrData( message ,envId );
                        }
                        else if ( !Strings.isNullOrEmpty( peerId ) )
                        {
                            decrData( message ,envId );
                        }
                    }

                }
            }
        }
        catch(Exception ex)
        {

        }
    }



    /* ******************************************************
     *
     */
    private void decrData( Message message, String hostId )
    {

        InputStream is = message.getContent( InputStream.class );
        CachedOutputStream os = new CachedOutputStream();

        try
        {
            IOUtils.copyAndCloseInput( is, os );
            os.flush();

            byte[] data = decrData(hostId,os.getBytes());
            org.apache.commons.io.IOUtils.closeQuietly( os );

            if(data!=null)
            {
                message.setContent( InputStream.class, new ByteArrayInputStream( data ));
            }

        }
        catch ( IOException e )
        {
            LOG.error( "STEP 2 error", e );
        }
    }


    /* ******************************************************
     *
     */
    private byte[] decrData( String hostId, byte[] data )
    {

        try
        {
            if ( data == null || data.length == 0)
            {
                return null;
            }
            else
            {
                EncryptionTool encTool = channelManagerImpl.getSecurityManager().getEncryptionTool();
                KeyManager keyMan = channelManagerImpl.getSecurityManager().getKeyManager();
                PGPSecretKeyRing secKey = keyMan.getSecretKeyRing(  hostId );

                byte[] outData = encTool.decrypt( data, secKey, "12345678" );

                return outData;
            }
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    //******************************************************************
}
