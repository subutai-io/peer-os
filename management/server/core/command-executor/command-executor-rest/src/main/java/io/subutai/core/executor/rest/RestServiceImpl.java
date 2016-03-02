package io.subutai.core.executor.rest;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.EncryptedRequestWrapper;
import io.subutai.common.command.EncryptedResponseWrapper;
import io.subutai.common.command.Request;
import io.subutai.common.command.ResponseImpl;
import io.subutai.common.command.ResponseWrapper;
import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.HeartbeatListener;
import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.RestProcessor;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


//temporarily made rest-impl as subscription service for heartbeat listeners
//TODO extract separate service/class for this purpose^, move it to command-executor-impl
public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private final SecurityManager securityManager;
    private final RestProcessor restProcessor;

    protected Set<HeartbeatListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<HeartbeatListener, Boolean>() );
    protected ExecutorService notifier = Executors.newCachedThreadPool();


    public RestServiceImpl( final SecurityManager securityManager, final RestProcessor restProcessor )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( restProcessor );

        this.securityManager = securityManager;
        this.restProcessor = restProcessor;
    }


    public void dispose()
    {
        notifier.shutdown();
    }


    public void addListener( HeartbeatListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void removeListener( HeartbeatListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }


    @Override
    public Response processHeartbeat( final String heartbeat )
    {
        try
        {
            String decryptedHeartbeat = decrypt( heartbeat );

            final HeartBeat heartBeat = JsonUtil.fromJson( decryptedHeartbeat, HeartBeat.class );

            for ( final HeartbeatListener listener : listeners )
            {
                notifier.submit( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            listener.onHeartbeat( heartBeat );
                        }
                        catch ( Exception e )
                        {
                            LOG.error( "Error in processHeartbeat", e );
                        }
                    }
                } );
            }

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response processResponse( final String response )
    {
        try
        {
            String decryptedResponse = decrypt( response );

            ResponseWrapper responseWrapper = JsonUtil.fromJson( decryptedResponse, ResponseWrapper.class );

            final ResponseImpl responseImpl = responseWrapper.getResponse();

            restProcessor.handleResponse( responseImpl );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getRequests( String hostId )
    {
        try
        {
            Set<Request> hostRequests = restProcessor.getRequests( hostId );

            if ( CollectionUtil.isCollectionEmpty( hostRequests ) )
            {
                return Response.noContent().build();
            }
            else
            {
                return Response.ok( encrypt( JsonUtil.toJson( hostRequests ), hostId ) ).build();
            }
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    //todo move this method to SecurityManager
    protected String encrypt( String message, String hostId )
    {
        if ( SystemSettings.getEncryptionState() )
        {
            try
            {
                EncryptionTool encryptionTool = securityManager.getEncryptionTool();


                //obtain target host pub key for encrypting
                PGPPublicKey hostKeyForEncrypting = securityManager.getKeyManager().getPublicKey( hostId );

                String encryptedRequestString = new String( encryptionTool
                        .signAndEncrypt( JsonUtil.toJson( message ).getBytes(), hostKeyForEncrypting, true ) );

                EncryptedRequestWrapper encryptedRequestWrapper =
                        new EncryptedRequestWrapper( encryptedRequestString, hostId );

                return JsonUtil.toJson( encryptedRequestWrapper );
            }
            catch ( Exception e )
            {
                LOG.error( "Error in process", e );
            }
        }

        return message;
    }


    //todo move this method to SecurityManager
    protected String decrypt( String message ) throws PGPException
    {

        if ( SystemSettings.getEncryptionState() )
        {

            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            EncryptedResponseWrapper responseWrapper = JsonUtil.fromJson( message, EncryptedResponseWrapper.class );

            ContentAndSignatures contentAndSignatures =
                    encryptionTool.decryptAndReturnSignatures( responseWrapper.getResponse().getBytes() );

            PGPPublicKey hostKeyForVerifying =
                    securityManager.getKeyManager().getPublicKey( responseWrapper.getHostId() );

            if ( encryptionTool.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
            {
                message = new String( contentAndSignatures.getDecryptedContent() );
            }
            else
            {
                throw new IllegalArgumentException( String.format( "Verification failed%nDecrypted Message: %s",
                        new String( contentAndSignatures.getDecryptedContent() ) ) );
            }
        }

        return message;
    }
}
