package org.safehaus.subutai.plugin.hadoop.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    // get cluster list
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
    public Response installCluster( @PathParam("clusterName") String clusterName,
                                    @PathParam("numberOfSlaveNodes") int numberOfSlaveNodes,
                                    @PathParam("numberOfReplicas") int numberOfReplicas );

    //uninstall cluster
    @DELETE
    @Path("clusters/{clusterName}") //Maps for the `hello/John` in the URL
    @Produces({ MediaType.APPLICATION_JSON })
    public Response uninstallCluster( @PathParam("clusterName") String clusterName );

    //startNameNode
    @PUT
    @Path("clusters/{clusterName}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startNameNode( @PathParam("clusterName") String clusterName );

    //stopNameNode
    @PUT
    @Path("clusters/{clusterName}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopNameNode( @PathParam("clusterName") String clusterName );

    //statusNameNode
    @GET
    @Path("clusters/{clusterName}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response statusNameNode( @PathParam("clusterName") String clusterName );

    //statusSecondaryNameNode
    @GET
    @Path("clusters/{clusterName}/status/secondary")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response statusSecondaryNameNode( @PathParam("clusterName") String clusterName );

    //startJobTracker
    @PUT
    @Path("clusters/job/{clusterName}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startJobTracker( @PathParam("clusterName") String clusterName );

    //stopJobTracker
    @PUT
    @Path("clusters/job/{clusterName}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopJobTracker( @PathParam("clusterName") String clusterName );

    //statusJobTracker
    @GET
    @Path("clusters/job/{clusterName}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response statusJobTracker( @PathParam("clusterName") String clusterName );

    //addNode
    @POST
    @Path("clusters/{clusterName}/nodes")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNode( @PathParam("clusterName") String clusterName );

    //statusDataNode
    @GET
    @Path("clusters/{clusterName}/node/{hostname}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response statusDataNode( @PathParam("clusterName") String clusterName,
                                    @PathParam("hostname") String hostname );

    //statusTaskTracker
    @GET
    @Path("clusters/{clusterName}/task/{hostname}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response statusTaskTracker( @PathParam("clusterName") String clusterName,
                                       @PathParam("hostname") String hostname );
}