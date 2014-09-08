package org.safehaus.subutai.core.dispatcher.rest;


import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class RestServiceImpl implements RestService {

    private final CommandDispatcher dispatcher;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public RestServiceImpl( final CommandDispatcher dispatcher ) {
        this.dispatcher = dispatcher;
    }


    @Override
    public Response processResponses( final String responses ) {
        try {
            Set<org.safehaus.subutai.common.protocol.Response> resps =
                    gson.fromJson( responses, new TypeToken<LinkedHashSet<BatchRequest>>() {}.getType() );
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
            Set<BatchRequest> reqs = gson.fromJson( requests, new TypeToken<Set<BatchRequest>>() {}.getType() );
            dispatcher.executeRequests( ownrId, reqs );
            return Response.ok().build();
        }
        catch ( RuntimeException e ) {
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }
}
