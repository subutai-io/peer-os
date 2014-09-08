package org.safehaus.subutai.plugin.solr.rest;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


public interface RestService
{

    //list clusters
    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public String listClusters();

    //view cluster info
    @GET
    @Path("clusters/{clustername}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCluster( @PathParam("clustername") String clustername );

    //create cluster
    @POST
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCluster( @QueryParam("config") String config );

    //destroy cluster
    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String destroyCluster( @PathParam("clusterName") String clusterName );

    //start node
    @PUT
    @Path("clusters/{clusterName}/nodes/{lxchostname}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public String startNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //stop node
    @PUT
    @Path("clusters/{clusterName}/nodes/{lxchostname}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public String stopNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String destroyNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );

    //check node status
    @GET
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String checkNode( @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String lxchostname );
}