package io.subutai.core.channel.impl.interceptor;


import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.http.HttpResponse;

import com.google.common.base.Strings;

import io.subutai.common.security.utils.io.HexUtil;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Out Interceptor
 */
public class ServerOutInterceptor  extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ServerOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public ServerOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.POST_INVOKE );
        this.channelManagerImpl = channelManagerImpl;
    }


    /**
     * Intercepts a message.
     * interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        try
        {
            LOG.info( "Server OutInterceptor invoked ");

            URL url = new URL( ( String ) message.get( Message. REQUEST_URL ) );


            //***********************************************************************
            if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ))
            {
                HttpHeaders headers = new HttpHeadersImpl(message.getExchange().getInMessage());

                HttpServletResponse res = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);
                HttpResponse res2 = (HttpResponse) message.get("HTTP.REQUEST");

                String remoteIp = headers.getHeaderString( Common.SECURED_HEADER_NAME );

                if( !Strings.isNullOrEmpty( remoteIp ))
                {
                    String envId  = headers.getHeaderString( Common.ENVIRONMENT_ID_HEADER_NAME );
                    String peerId = headers.getHeaderString( Common.PEER_ID_HEADER_NAME);

                    OutputStream os = message.getContent(OutputStream.class);

                    //responseCode = (Integer) message.get(Message.RESPONSE_CODE);

                    String  str  = (String)message.get(Message.INBOUND_MESSAGE);

                    if(!Strings.isNullOrEmpty( envId ))
                    {
                        String encryptedData = encryptData( envId,remoteIp,"ENCRYPTED_DATA TEST");
                    }
                    else if(!Strings.isNullOrEmpty( peerId ))
                    {
                        String encryptedData = encryptData( peerId,remoteIp,"ENCRYPTED_DATA TEST");
                    }
                }
            }
            //***********************************************************************


        }
        catch(Exception ex)
        {

        }
    }


    /* ******************************************************
     *
     */
    private String encryptData(String hostId,String ip,String dataStr)
    {
        EncryptionTool encTool = channelManagerImpl.getSecurityManager().getEncryptionTool();
        KeyManager keyMan      = channelManagerImpl.getSecurityManager().getKeyManager();
        PGPPublicKey pubKey    = keyMan.getRemoteHostPublicKey ( hostId , ip );

        byte[]data = encTool.encrypt(dataStr.getBytes(),pubKey,false );

        return HexUtil.byteArrayToHexString( data );
    }


    /* ******************************************************
     *
     */
}
