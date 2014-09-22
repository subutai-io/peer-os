package org.safehaus.subutai.core.dispatcher.impl;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.gson.JsonSyntaxException;


/**
 * Sends responses produced by remote requests back to owner
 */
public class ResponseSender
{
    private static final Logger LOG = Logger.getLogger( ResponseSender.class.getName() );

    private static final int SLEEP_BETWEEN_ITERATIONS_SEC = 1;
    private static final int AGENT_CHUNK_SEND_INTERVAL_SEC = 20;
    private static final int RETRY_ATTEMPT_WIDENING_INTERVAL_SEC = 30;
    private static final int SELECT_RECORDS_LIMIT = 50;
    private final ExecutorService mainLoopExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService httpRequestsExecutor = Executors.newCachedThreadPool();
    private final DispatcherDAO dispatcherDAO;
    private final PeerManager peerManager;


    public ResponseSender( final DispatcherDAO dispatcherDAO, final PeerManager peerManager )
    {

        this.dispatcherDAO = dispatcherDAO;
        this.peerManager = peerManager;
    }


    public void init()
    {
        mainLoopExecutor.submit( new Runnable()
        {
            @Override
            public void run()
            {

                while ( !Thread.interrupted() )
                {
                    try
                    {
                        Thread.sleep( SLEEP_BETWEEN_ITERATIONS_SEC * 1000L );
                    }
                    catch ( InterruptedException e )
                    {
                        break;
                    }
                    send();
                }
            }
        } );
    }


    public void dispose()
    {
        mainLoopExecutor.shutdown();
        httpRequestsExecutor.shutdown();
    }


    private int calculateOfAttempts()
    {
        int attempts = 0;
        int inactiveCommandDropTimeoutSec =
                org.safehaus.subutai.common.settings.Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC;
        while ( inactiveCommandDropTimeoutSec > 0 )
        {
            attempts++;
            inactiveCommandDropTimeoutSec -= attempts * RETRY_ATTEMPT_WIDENING_INTERVAL_SEC;
        }
        return attempts;
    }


    private void send()
    {

        try
        {
            Set<RemoteRequest> requests =
                    dispatcherDAO.getRemoteRequests( calculateOfAttempts(), SELECT_RECORDS_LIMIT );

            if ( !requests.isEmpty() )
            {
                List<Callable<Object>> todo = new ArrayList<>( requests.size() );

                for ( final RemoteRequest request : requests )
                {
                    todo.addAll( sendRequest( request ) );
                }

                if ( !todo.isEmpty() )
                {
                    httpRequestsExecutor.invokeAll( todo );
                }
            }
        }
        catch ( InterruptedException | DBException e )
        {
            LOG.log( Level.SEVERE, "Error in send", e );
        }
    }


    private List<Callable<Object>> sendRequest( final RemoteRequest request )
    {
        List<Callable<Object>> todo = new ArrayList<>();
        try
        {
            final Set<RemoteResponse> responses = dispatcherDAO.getRemoteResponses( request.getCommandId() );

            if ( responses.isEmpty() )
            {
                //delete request and responses after inactivity timeout reached
                if ( System.currentTimeMillis() - request.getTimestamp()
                        > org.safehaus.subutai.common.settings.Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000L )
                {
                    dispatcherDAO.deleteRemoteRequest( request.getCommandId() );
                    dispatcherDAO.deleteRemoteResponses( request.getCommandId() );
                }

                //if no responses arrived to this request within agent notification response interval,
                // increment its attempts' number
                else if ( System.currentTimeMillis() - request.getTimestamp()
                        > ( AGENT_CHUNK_SEND_INTERVAL_SEC + 5 ) * 1000L )
                {
                    request.incrementAttempts();
                    dispatcherDAO.saveRemoteRequest( request );
                    //delete previous request (workaround until we change Cassandra to another DB)
                    dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
                }
            }
            else
            {
                //add task to send responses
                todo.add( Executors.callable( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sendResponses( request, responses );
                    }
                } ) );
            }
        }
        catch ( DBException e )
        {
            LOG.log( Level.SEVERE, "Error in sendRequest", e );
        }

        return todo;
    }


    private void sendResponses( RemoteRequest request, Set<RemoteResponse> responses )
    {
        try
        {
            //sort responses by responseNumber
            Set<Response> sortedResponses = new TreeSet<>( new Comparator<Response>()
            {
                @Override
                public int compare( final Response o1, final Response o2 )
                {
                    int compareAgents = o1.getUuid().compareTo( o2.getUuid() );
                    return compareAgents == 0 ?
                           o1.getResponseSequenceNumber().compareTo( o2.getResponseSequenceNumber() ) : compareAgents;
                }
            } );
            for ( RemoteResponse response : responses )
            {
                sortedResponses.add( response.getResponse() );
            }

            sendResponse( request, responses, sortedResponses );
        }
        catch ( JsonSyntaxException | DBException e )
        {
            LOG.log( Level.SEVERE, "Error in sendResponses", e );
        }
    }


    private void sendResponse( RemoteRequest request, Set<RemoteResponse> responses, Set<Response> sortedResponses )
            throws DBException
    {
        try
        {
            Peer peer = peerManager.getPeerByUUID( request.getPeerId() );

            String message =
                    JsonUtil.toJson( new DispatcherMessage( sortedResponses, DispatcherMessageType.RESPONSE ) );
            peerManager.sendPeerMessage( peer, Common.DISPATCHER_NAME, message );

            //delete sent responses
            for ( RemoteResponse response : responses )
            {

                dispatcherDAO.deleteRemoteResponse( response );

                if ( response.getResponse().isFinal() )
                {
                    request.incrementCompletedRequestsCount();
                }
            }
            //if final response was sent, delete request
            if ( request.isCompleted() )
            {
                dispatcherDAO.deleteRemoteRequest( request.getCommandId() );
            }
            else
            {
                request.updateTimestamp();
                dispatcherDAO.saveRemoteRequest( request );
            }
        }

        catch ( PeerMessageException e )
        {
            LOG.log( Level.SEVERE, "Error in sendResponses", e );

            //increment attempts based on widening intervals
            if ( System.currentTimeMillis() - request.getTimestamp()
                    > request.getAttempts() * RETRY_ATTEMPT_WIDENING_INTERVAL_SEC * 1000L )
            {
                request.incrementAttempts();
                dispatcherDAO.saveRemoteRequest( request );
                //delete previous request (workaround until we change Cassandra to another DB)
                dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
            }
        }
    }
}
