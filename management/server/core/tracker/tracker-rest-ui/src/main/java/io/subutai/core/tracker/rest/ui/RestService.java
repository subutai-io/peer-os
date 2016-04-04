package io.subutai.core.tracker.rest.ui;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @GET
    @Path( "operations/{source}/{uuid}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getTrackerOperation(@PathParam("source") String source, @PathParam("uuid") String uuid);


    @GET
    @Path( "operations/{source}/{dateFrom}/{dateTo}/{limit}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getTrackerOperations(@PathParam("source") String source, @PathParam("dateFrom") String fromDate,
                                         @PathParam("dateTo") String toDate, @PathParam("limit") int limit);

    @GET
    @Path( "operations/sources" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getTrackerOperationSources();


    @GET
    @Path( "notifications" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getNotification();


    @DELETE
    @Path( "notifications/{source}/{uuid}" )
    Response clearNotification( @PathParam("source") String source, @PathParam("uuid") String uuid );

    @DELETE
    @Path( "notifications" )
    Response clearAllNotifications();
}