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
    @Produces( { MediaType.TEXT_PLAIN } )
    Response processRegisterRequest( @FormParam( "ip" ) String ip, @FormParam( "key_phrase" ) String keyPhrase );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegisteredPeers();

    @GET
    @Path( "states" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegisteredPeersStates();

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
    @Path( "rename" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response renamePeer( @FormParam( "peerId" ) String peerId, @FormParam( "name" ) String name );

    @PUT
    @Path( "url" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response updatePeerUrl( @FormParam( "peerId" ) String peerId, @FormParam( "ip" ) String ip );

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
    @Produces( { MediaType.TEXT_PLAIN } )
    Response checkPeer( @FormParam( "ip" ) String ip );
}