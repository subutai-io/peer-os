package org.safehaus.subutai.plugin.zookeeper.rest;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    //list clusters
    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response listClusters();

    //view cluster info
    @GET
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCluster(@PathParam("clusterName") String source);

    //create cluster
    @POST
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response createCluster( @QueryParam("config") String config );

    //destroy cluster
    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyCluster( @PathParam("clusterName") String clusterName );

    //start node
    @PUT
    @Path("clusters/{clusterName}/nodes/{lxcHostname}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startNode( @PathParam("clusterName") String clusterName,
                               @PathParam("lxcHostname") String lxcHostname);

    //stop node
    @PUT
    @Path("clusters/{clusterName}/nodes/{lxcHostname}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopNode( @PathParam("clusterName") String clusterName,
                              @PathParam("lxcHostname") String lxcHostname);

    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxcHostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyNode( @PathParam("clusterName") String clusterName,
                                 @PathParam("lxcHostname") String lxcHostname);

    //check node status
    @GET
    @Path("clusters/{clusterName}/nodes/{lxcHostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response checkNode( @PathParam("clusterName") String clusterName,
                               @PathParam("lxcHostname") String lxcHostname);

    //add node over existing node
    @POST
    @Path("clusters/{clusterName}/nodes/{lxcHostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNode( @PathParam("clusterName") String clusterName,
                             @PathParam("lxcHostname") String lxcHostname);

    //add node standalone
    @POST
    @Path("clusters/{clusterName}/nodes")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNodeStandalone( @PathParam("clusterName") String clusterName );
}