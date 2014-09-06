package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl implements CommandDispatcher {


    public void init() {}


    public void destroy() {}


    @Override
    public void sendRequests( final Map<UUID, Request> requests ) {

    }


    @Override
    public void addListener( final ResponseListener listener ) {

    }


    @Override
    public void removeListener( final ResponseListener listener ) {

    }


    @Override
    public void processResponse( final String response ) {

    }


    @Override
    public void executeRequests( final Set<Request> requests ) {

    }
}
