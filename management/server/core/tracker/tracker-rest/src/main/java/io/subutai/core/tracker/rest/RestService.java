package io.subutai.core.tracker.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @GET
    @Path( "operations/{source}/{uuid}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTrackerOperation( @PathParam("source") String source, @PathParam("uuid") String uuid );


    @GET
    @Path( "operations/{source}/{dateFrom}/{dateTo}/{limit}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTrackerOperations( @PathParam("source") String source, @PathParam("dateFrom") String fromDate,
                                          @PathParam("dateTo") String toDate, @PathParam("limit") int limit );

    @GET
    @Path( "operations/sources" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTrackerOperationSources();


    @GET
    @Path( "subutai/about" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getSubutaiInfo();

}