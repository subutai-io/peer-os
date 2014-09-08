package org.safehaus.subutai.core.dispatcher.rest;


import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;


public class RestServiceImpl implements RestService {

    private final CommandDispatcher dispatcher;


    public RestServiceImpl( final CommandDispatcher dispatcher ) {
        this.dispatcher = dispatcher;
    }


    @Override
    public Response processResponses( final String responses ) {
        try {
            Set<org.safehaus.subutai.common.protocol.Response> resps =
                    JsonUtil.fromJson( responses, LinkedHashSet.class );
            dispatcher.processResponses( resps );
            return Response.ok().build();
        }
        catch ( RuntimeException e ) {
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response executeRequests( final String ownerId, final String requests ) {
        try {
            UUID ownrId = JsonUtil.fromJson( ownerId, UUID.class );
            Set<BatchRequest> reqs = JsonUtil.fromJson( requests, Set.class );
            dispatcher.executeRequests( ownrId, reqs );
            return Response.ok().build();
        }
        catch ( RuntimeException e ) {
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }
}
