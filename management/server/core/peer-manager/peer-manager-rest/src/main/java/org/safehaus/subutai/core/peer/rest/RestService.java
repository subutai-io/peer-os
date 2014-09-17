package org.safehaus.subutai.core.peer.rest;


import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.message.Common;


@WebService(serviceName = "peerServices")
public interface RestService {


    @POST
    @Path("/peer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Peer registerPeer( String peer );

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPeerJsonFormat();

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String getId();


    @POST
    @Path( Common.MESSAGE_REQUEST_SUB_PATH )
    public Response processMessage( @FormParam( Common.PEER_ID_PARAM_NAME ) String peerId,
                                    @FormParam( Common.MESSAGE_PARAM_NAME ) String message );
}