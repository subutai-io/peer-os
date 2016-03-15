package io.subutai.core.executor.rest;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.ResponseImpl;
import io.subutai.common.command.ResponseWrapper;
import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.HeartbeatListener;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.RestProcessor;
import io.subutai.core.security.api.SecurityManager;


//todo temporarily made rest-impl as subscription service for heartbeat listeners
//todo extract separate service/class for this purpose^, move it to command-executor-impl
//todo close the URLs for 8444 port only in AccessControlInterceptor
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
            Set<String> hostRequests = restProcessor.getRequests( hostId );

            if ( CollectionUtil.isCollectionEmpty( hostRequests ) )
            {
                LOG.info( String.format( "Requested commands for RH %s. No requests", hostId ) );
                return Response.noContent().build();
            }
            else
            {
                LOG.info( String.format( "Requested commands for RH %s. Requests: %s", hostId,
                        hostRequests.toString() ) );
                return Response.ok( hostRequests.toString() ).build();
            }
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response check( final String hostId )
    {
        if ( securityManager.getKeyManager().getPublicKey( hostId ) != null )
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    protected String decrypt( String message ) throws PGPException
    {
        return securityManager.decryptNVerifyResponseFromHost( message );
    }
}
