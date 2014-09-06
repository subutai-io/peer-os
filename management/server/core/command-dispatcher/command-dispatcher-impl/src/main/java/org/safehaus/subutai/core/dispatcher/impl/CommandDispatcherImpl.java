package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl implements CommandDispatcher {
    private static final Logger LOG = Logger.getLogger( CommandDispatcherImpl.class.getName() );

    private final Queue<ResponseListener> listeners = new ConcurrentLinkedQueue<>();


    public void init() {}


    public void destroy() {}


    @Override
    public void sendRequests( final Map<UUID, Set<BatchRequest>> requests ) {

        //use PeerManager to figure out IP of target peer by UUID
        //send requests in batch to each peer
    }


    @Override
    public void addListener( final ResponseListener listener ) {
        try {
            if ( !listeners.contains( listener ) ) {
                listeners.add( listener );
            }
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error to add a listener:", ex );
        }
    }


    @Override
    public void removeListener( final ResponseListener listener ) {
        try {
            listeners.remove( listener );
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in removeListener", ex );
        }
    }


    @Override
    public void processResponse( final Set<Response> responses ) {
        //trigger CommandRunner.onResponse
    }


    @Override
    public void executeRequests( final UUID initiatorId, final Set<BatchRequest> requests ) {
        //execute requests using Command Runner
        //upon response in callback, cache response to persistent queue (key => initiator id)
        //some background thread should iterate this queue and attempt to send responses back to initiator
    }
}
