package io.subutai.core.peer.rest.ui;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    Response processRegisterRequest( @FormParam( "ip" ) String ip, @FormParam( "key_phrase" ) String keyPhrase );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegisteredPeers();

    @PUT
    @Path( "reject" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response rejectForRegistrationRequest( @FormParam( "peerId" ) String peerId );

    @PUT
    @Path( "approve" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response approveForRegistrationRequest( @FormParam( "peerId" ) String peerId,
                                            @FormParam( "key_phrase" ) String keyPhrase );

    @PUT
    @Path( "cancel" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response cancelForRegistrationRequest( @FormParam( "peerId" ) String peerId );

    @PUT
    @Path( "unregister" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response unregisterForRegistrationRequest( @FormParam( "peerId" ) String peerId );


    @GET
    @Path( "resource_hosts" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getResourceHosts( );
}