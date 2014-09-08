package org.safehaus.subutai.core.dispatcher.rest;


import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService {

    @PUT
    @Path( "responses" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response processResponses( @QueryParam( "responses" ) String responses );

    @PUT
    @Path( "requests" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response executeRequests( @QueryParam( "ownerId" ) String ownerId,
                                     @QueryParam( "requests" ) String requests );
}
