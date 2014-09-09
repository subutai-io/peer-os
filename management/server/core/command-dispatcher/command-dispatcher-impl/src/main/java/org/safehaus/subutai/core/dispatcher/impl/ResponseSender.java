package org.safehaus.subutai.core.dispatcher.impl;


import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.db.api.DBException;

import com.google.common.base.Preconditions;


/**
 * Sends responses produced by remote requests back to owner
 */
public class ResponseSender {
    private static final Logger LOG = Logger.getLogger( ResponseSender.class.getName() );

    private static final int RESPONSE_OK = 200;
    private static final int SLEEP_BETWEEN_ITERATIONS_SEC = 5;
    private static final int AGENT_CHUNK_SEND_INTERVAL_SEC = 15;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DispatcherDAO dispatcherDAO;
    private final HttpUtil httpUtil;


    public ResponseSender( final DispatcherDAO dispatcherDAO, HttpUtil httpUtil ) {
        Preconditions.checkNotNull( dispatcherDAO, "DispatcherDAO is null" );

        this.dispatcherDAO = dispatcherDAO;
        this.httpUtil = httpUtil;
    }


    public void init() {
        executor.submit( new Runnable() {
            @Override
            public void run() {

                while ( !Thread.interrupted() ) {

                    try {
                        Thread.sleep( SLEEP_BETWEEN_ITERATIONS_SEC * 1000 );
                    }
                    catch ( InterruptedException e ) {
                        break;
                    }

                    send();
                }
            }
        } );
    }


    public void dispose() {
        executor.shutdown();
    }


    private void send() {

        try {
            Set<RemoteRequest> requests = dispatcherDAO.getRemoteRequests( 10, 20 );

            for ( RemoteRequest request : requests ) {
                try {
                    Set<RemoteResponse> responses = dispatcherDAO.getRemoteResponses( request.getCommandId() );

                    //if no responses arrived to this request, increment is attempts number
                    if ( responses.isEmpty() && ( System.currentTimeMillis() - request.getTimestamp()
                            > AGENT_CHUNK_SEND_INTERVAL_SEC * 1000 + 2000 ) ) {
                        request.incrementAttempts();
                        dispatcherDAO.saveRemoteRequest( request );
                        //delete previous request (workaround until we change Cassandra to another DB)
                        dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
                    }
                    else {
                        //sort responses by responseNumber
                        Set<Response> sortedSet = new TreeSet<>( new Comparator<Response>() {
                            @Override
                            public int compare( final Response o1, final Response o2 ) {
                                return o1.getResponseSequenceNumber() - o2.getResponseSequenceNumber();
                            }
                        } );
                        for ( RemoteResponse response : responses ) {
                            sortedSet.add( response.getResponse() );
                        }
                        //fill http params
                        Map<String, String> params = new HashMap<>();
                        params.put( "responses", JsonUtil.toJson( sortedSet ) );
                        //try to send responses to PEER
                        int responseCode = 0;
                        try {
                            responseCode = httpUtil.httpLitePost( String.format( Common.RESPONSE_URL, request.getIp() ),
                                    params );
                            if ( responseCode == RESPONSE_OK ) {
                                //delete sent responses
                                boolean isFinalResponseSent = false;
                                for ( RemoteResponse response : responses ) {
                                    dispatcherDAO.deleteRemoteResponse( response );
                                    if ( response.getResponse().isFinal() ) {
                                        isFinalResponseSent = true;
                                    }
                                }
                                //if final response was sent, delete request
                                if ( isFinalResponseSent ) {
                                    request.incrementCompletedRequestsCount();
                                    if ( request.isCompleted() ) {
                                        dispatcherDAO
                                                .deleteRemoteRequest( request.getCommandId(), request.getAttempts() );
                                    }
                                    else {
                                        dispatcherDAO.saveRemoteRequest( request );
                                    }
                                }
                            }
                            else {


                                LOG.log( Level.WARNING,
                                        String.format( "Error sending response to %s: error code = %s", request.getIp(),
                                                responseCode ) );
                            }
                        }
                        catch ( IOException e ) {
                            LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
                        }

                        if ( responseCode != RESPONSE_OK ) {
                            //increment attempts
                            request.incrementAttempts();
                            dispatcherDAO.saveRemoteRequest( request );
                            //delete previous request (workaround until we change Cassandra to another DB)
                            dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
                        }
                    }
                }
                catch ( DBException e ) {
                    LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
                }
            }
        }
        catch ( DBException e ) {
            LOG.log( Level.SEVERE, String.format( "Error in send: %s", e.getMessage() ) );
        }
    }
}
