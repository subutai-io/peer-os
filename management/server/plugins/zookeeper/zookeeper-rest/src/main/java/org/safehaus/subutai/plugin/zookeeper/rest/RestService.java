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
    @Path("clusters/{clustername}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCluster( @PathParam("clustername") String source );

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
    @Path("clusters/{clusterName}/nodes/{lxchostname}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //stop node
    @PUT
    @Path("clusters/{clusterName}/nodes/{lxchostname}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //check node status
    @GET
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response checkNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //add node over existing node
    @POST
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //add node standalone
    @POST
    @Path("clusters/{clusterName}/nodes")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNodeStandalone( @PathParam("clusterName") String clusterName );
}