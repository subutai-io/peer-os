package org.safehaus.subutai.plugin.accumulo.rest;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


public interface RestService
{
    //list clusters
    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response listClusters();

    //view cluster info
    @GET
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response getCluster(@PathParam("clusterName") String clusterName);

    //create cluster
    @POST
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response createCluster(@QueryParam("config") String config);

    //destroy cluster
    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response destroyCluster(@PathParam("clusterName") String clusterName);

    //start cluster
    @PUT
    @Path("clusters/{clusterName}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response startCluster(@PathParam("clusterName") String clusterName);

    //stop cluster
    @PUT
    @Path("clusters/{clusterName}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response stopCluster(@PathParam("clusterName") String clusterName);

    //add node
    @POST
    @Path("clusters/{clusterName}/nodes/{lxcHostname}/{nodeType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response addNode(@PathParam("clusterName") String clusterName,
                                             @PathParam("lxcHostname") String lxcHostname, @PathParam("nodeType") String nodeType);

    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxcHostname}/{nodeType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response destroyNode(@PathParam("clusterName") String clusterName,
                                                 @PathParam("lxcHostname") String lxcHostname,
                                                 @PathParam("nodeType") String nodeType);

    //check node status
    @GET
    @Path("clusters/{clusterName}/nodes/{lxcHostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public javax.ws.rs.core.Response checkNode(@PathParam("clusterName") String clusterName,
                                               @PathParam("lxcHostname") String lxcHostname);
}