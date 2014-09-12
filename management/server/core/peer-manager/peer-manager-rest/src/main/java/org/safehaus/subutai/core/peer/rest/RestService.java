package org.safehaus.subutai.core.peer.rest;


import org.safehaus.subutai.core.peer.api.Peer;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@WebService(serviceName = "peerServices")
public interface RestService {


    @POST
    @Path("/peer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Peer registerPeer(String peer);

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPeerJsonFormat();

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String getId();
}