package io.subutai.core.peer.rest.ui;


import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.protocol.N2NConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//todo please check all endpoints for returned media type, do we return correct type if we just return response code
// then no need to indicate it at all!!!


public interface RestService
{
    @POST
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response processRegisterRequest( @FormParam( "ip" ) String ip, @FormParam( "key_phrase" ) String KeyPhrase );
    //public Response processRegisterRequest( @FormParam( "peer" ) String peer );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegisteredPeers();

    @PUT
    @Path( "reject" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response rejectForRegistrationRequest( @FormParam( "rejectedPeerId" ) String rejectedPeerId );

    @PUT
    @Path( "approve" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response approveForRegistrationRequest( @FormParam( "approvePeerId" ) String approvePeerId );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegisteredPeerInfo( @QueryParam( "peerId" ) String peerId );
}