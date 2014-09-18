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
public class ResponseSender {
    private static final Logger LOG = Logger.getLogger( ResponseSender.class.getName() );

    private static final int SLEEP_BETWEEN_ITERATIONS_SEC = 1;
    private static final int AGENT_CHUNK_SEND_INTERVAL_SEC = 15;
    private final ExecutorService mainLoopExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService httpRequestsExecutor = Executors.newCachedThreadPool();
    private final DispatcherDAO dispatcherDAO;
    private final PeerManager peerManager;


    public ResponseSender( final DispatcherDAO dispatcherDAO, final PeerManager peerManager ) {

        this.dispatcherDAO = dispatcherDAO;
        this.peerManager = peerManager;
    }


    public void init() {
        mainLoopExecutor.submit( new Runnable() {
            @Override
            public void run() {

                while ( !Thread.interrupted() )
                {

                    try
                    {
                        Thread.sleep( SLEEP_BETWEEN_ITERATIONS_SEC * 1000 );
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


    public void dispose() {
        mainLoopExecutor.shutdown();
        httpRequestsExecutor.shutdown();
    }


    private void send() {

        try
        {
            Set<RemoteRequest> requests = dispatcherDAO.getRemoteRequests( 10, 20 );

            if ( !requests.isEmpty() )
            {

                List<Callable<Object>> todo = new ArrayList<>( requests.size() );


                for ( final RemoteRequest request : requests )
                {
                    try
                    {
                        final Set<RemoteResponse> responses =
                                dispatcherDAO.getRemoteResponses( request.getCommandId() );

                        //if no responses arrived to this request, increment is attempts number
                        if ( responses.isEmpty() && ( System.currentTimeMillis() - request.getTimestamp()
                                > ( AGENT_CHUNK_SEND_INTERVAL_SEC + 5 ) * 1000 ) )
                        {
                            request.incrementAttempts();
                            dispatcherDAO.saveRemoteRequest( request );
                            //delete previous request (workaround until we change Cassandra to another DB)
                            dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
                        }
                        else if ( !responses.isEmpty() )
                        {

                            //add task to send responses
                            todo.add( Executors.callable( new Runnable() {
                                @Override
                                public void run() {
                                    sendResponses( request, responses );
                                }
                            } ) );
                        }
                    }
                    catch ( DBException e )
                    {
                        LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
                    }
                }

                if ( !todo.isEmpty() )
                {
                    httpRequestsExecutor.invokeAll( todo );
                }
            }
        }
        catch ( InterruptedException | DBException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
        }
    }


    private void sendResponses( RemoteRequest request, Set<RemoteResponse> responses ) {
        try
        {
            //sort responses by responseNumber
            Set<Response> sortedSet = new TreeSet<>( new Comparator<Response>() {
                @Override
                public int compare( final Response o1, final Response o2 ) {
                    int compareAgents = o1.getUuid().compareTo( o2.getUuid() );
                    return compareAgents == 0 ?
                           o1.getResponseSequenceNumber().compareTo( o2.getResponseSequenceNumber() ) : compareAgents;
                }
            } );
            for ( RemoteResponse response : responses )
            {
                sortedSet.add( response.getResponse() );
            }

            try
            {
                Peer peer = peerManager.getPeerByUUID( request.getPeerId() );

                String message = JsonUtil.toJson( new DispatcherMessage( sortedSet, DispatcherMessageType.RESPONSE ) );
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
                    dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() );
                }
                else
                {
                    request.updateTimestamp();
                    dispatcherDAO.saveRemoteRequest( request );
                }
            }

            catch ( PeerMessageException e )
            {
                LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
                //increment attempts
                request.incrementAttempts();
                dispatcherDAO.saveRemoteRequest( request );
                //delete previous request (workaround until we change Cassandra to another DB)
                dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
            }
        }
        catch ( JsonSyntaxException | DBException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in sendResponses: %s", e.getMessage() ) );
        }
    }
}
