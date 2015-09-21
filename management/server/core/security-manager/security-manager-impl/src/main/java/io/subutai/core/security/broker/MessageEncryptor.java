package io.subutai.core.security.broker;


import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.broker.api.TextMessagePostProcessor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );
    private static final String CLONE_CMD_REGEX = "\\s*subutai\\s*clone[\\s\\S]*?";

    private final boolean encryptionEnabled;


    public MessageEncryptor( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }


    public static SecurityManager getSecurityManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( SecurityManager.class );
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    public static PeerManager getPeerManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( PeerManager.class );
    }


    @Override
    public String process( final String topic, final String message )
    {
        LOG.debug( String.format( "OUTGOING %s", message ) );

        //assume this is a host  topic
        if ( encryptionEnabled && UUIDUtil.isStringAUuid( topic ) )
        {
            try
            {
                EncryptionTool encryptionTool = getSecurityManager().getEncryptionTool();

                RequestWrapper requestWrapper = JsonUtil.fromJson( message, RequestWrapper.class );

                Request originalRequest = requestWrapper.getRequest();

                //obtain target host pub key for encrypting
                PGPPublicKey hostKeyForEncrypting =
                        MessageEncryptor.getSecurityManager().getKeyManager().getPublicKey( originalRequest.getId().toString() );

                if ( originalRequest.getCommand().toLowerCase().matches( CLONE_CMD_REGEX ) )
                {
                    //add token for container creation
                    List<String> args = Lists.newArrayList( originalRequest.getArgs() );
                    args.add( "-t" );
                    args.add( getRegistrationManager().generateContainerTTLToken(
                            ( originalRequest.getTimeout() + Common.WAIT_CONTAINER_CONNECTION_SEC + 10 ) * 1000L )
                                                      .getToken() );

                    originalRequest =
                            new RequestBuilder.RequestImpl( originalRequest.getType(), originalRequest.getId(),
                                    originalRequest.getCommandId(), originalRequest.getWorkingDirectory(),
                                    originalRequest.getCommand(), args, originalRequest.getEnvironment(),
                                    originalRequest.getStdOut(), originalRequest.getStdErr(),
                                    originalRequest.getRunAs(), originalRequest.getTimeout(),
                                    originalRequest.isDaemon(), originalRequest.getConfigPoints(),
                                    originalRequest.getPid() );
                }

                String encryptedRequestString = new String( encryptionTool
                        .signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), hostKeyForEncrypting, true ) );

                EncryptedRequestWrapper encryptedRequestWrapper =
                        new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

                return JsonUtil.toJson( encryptedRequestWrapper );
            }
            catch ( Exception e )
            {
                LOG.error( "Error in process", e );
            }
        }

        return message;
    }
}
