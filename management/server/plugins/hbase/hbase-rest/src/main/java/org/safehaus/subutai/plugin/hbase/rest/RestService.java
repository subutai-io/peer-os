package org.safehaus.subutai.plugin.hbase.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public Response getCluster( @PathParam("clusterName") String clusterName );

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

    //start cluster
    @PUT
    @Path("clusters/{clusterName}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startCluster( @PathParam("clusterName") String clusterName );

    //stop cluster
    @PUT
    @Path("clusters/{clusterName}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopCluster( @PathParam("clusterName") String clusterName );

    //add node
    @POST
    @Path("clusters/{clusterName}/nodes/{containerId}/{nodeType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNode( @PathParam("clusterName") String clusterName, @PathParam("nodeType") String nodeType );

    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{containerId}/{nodeType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyNode( @PathParam("clusterName") String clusterName,
                                 @PathParam("containerId") String containerId, @PathParam("nodeType") String nodeType );

    //check node status
    @GET
    @Path("clusters/{clusterName}/nodes/{containerId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response checkNode( @PathParam("clusterName") String clusterName,
                               @PathParam("containerId") String containerId );
}