package io.subutai.core.peer.rest.ui;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    @Produces( { MediaType.TEXT_PLAIN } )
    Response processRegisterRequest( @FormParam( "ip" ) String ip, @FormParam( "key_phrase" ) String keyPhrase );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegisteredPeers();

    @PUT
    @Path( "reject" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response rejectForRegistrationRequest( @FormParam( "peerId" ) String peerId, @FormParam( "force" ) Boolean force );

    @PUT
    @Path( "approve" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response approveForRegistrationRequest( @FormParam( "peerId" ) String peerId,
                                            @FormParam( "key_phrase" ) String keyPhrase );

    @PUT
    @Path( "cancel" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response cancelForRegistrationRequest( @FormParam( "peerId" ) String peerId, @FormParam( "force" ) Boolean force );

    @PUT
    @Path( "unregister" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response unregisterForRegistrationRequest( @FormParam( "peerId" ) String peerId,
                                               @FormParam( "force" ) Boolean force );


    @GET
    @Path( "resource_hosts" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getResourceHosts();

    @GET
    @Path( "check" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response checkPeer( @FormParam( "ip" ) String ip );
}