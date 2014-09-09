package org.safehaus.subutai.core.dispatcher.impl;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.core.db.api.DBException;

import com.google.common.base.Preconditions;


/**
 * Created by dilshat on 9/9/14.
 */
public class ResponseSender {
    private static final Logger LOG = Logger.getLogger( ResponseSender.class.getName() );

    private static final int SLEEP_BETWEEN_ITERATIONS_SEC = 5;
    private static final int AGENT_CHUNK_SEND_INTERVAL_SEC = 15;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DispatcherDAO dispatcherDAO;


    public ResponseSender( final DispatcherDAO dispatcherDAO ) {
        Preconditions.checkNotNull( dispatcherDAO, "DispatcherDAO is null" );

        this.dispatcherDAO = dispatcherDAO;
    }


    public void start() {
        executor.submit( new Runnable() {
            @Override
            public void run() {

                send();

                while ( !Thread.interrupted() ) {
                    try {
                        Thread.sleep( SLEEP_BETWEEN_ITERATIONS_SEC * 1000 );
                    }
                    catch ( InterruptedException e ) {
                        break;
                    }
                }
            }
        } );
    }


    public void dispose() {
        executor.shutdown();
    }


    private void send() {

        try {
            List<RemoteRequest> requests = dispatcherDAO.getRemoteRequests( 10, 20 );

            for ( RemoteRequest request : requests ) {
                try {
                    List<RemoteResponse> responses = dispatcherDAO.getRemoteResponses( request.getCommandId() );

                    if ( responses.isEmpty() && ( System.currentTimeMillis() - request.getTimestamp()
                            > AGENT_CHUNK_SEND_INTERVAL_SEC * 1000 + 1000 ) ) {
                        request.incrementAttempts();
                        dispatcherDAO.saveRemoteRequest( request );
                        dispatcherDAO.deleteRemoteRequest( request.getCommandId(), request.getAttempts() - 1 );
                    } else{
                        //try to send responses to PEER
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
