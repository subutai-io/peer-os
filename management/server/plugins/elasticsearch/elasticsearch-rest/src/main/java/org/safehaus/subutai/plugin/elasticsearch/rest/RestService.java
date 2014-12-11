package org.safehaus.subutai.plugin.elasticsearch.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by ermek on 12/8/14.
 */
public interface RestService
{
    @GET
    @Path("clusters")
    @Produces({MediaType.APPLICATION_JSON})
    public Response listClusters();

    @POST
    @Path("clusters")
    @Produces({MediaType.APPLICATION_JSON})
    public Response installCluster(@PathParam("clusterName") String clusterName,
                                   @QueryParam("numberOfNodes") int numberOfNodes);

    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response uninstallCluster(@PathParam("clusterName") String clusterName);

    @GET
    @Path("clusters/{clusterName}/nodes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response checkAllNodes(@PathParam("clusterName") String clusterName);

    @PUT
    @Path("clusters/{clusterName}/nodes/start")
    @Produces({MediaType.APPLICATION_JSON})
    public Response startAllNodes(@PathParam("clusterName") String clusterName);

    @PUT
    @Path("clusters/{clusterName}/nodes/stop")
    @Produces({MediaType.APPLICATION_JSON})
    public Response stopAllNodes(@PathParam("clusterName") String clusterName);

    @POST
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addNode(@PathParam("clusterName") String clusterName, @PathParam("node") String node);

    @DELETE
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response destroyNode(@PathParam("clusterName") String clusterName, @PathParam("node") String node);

}