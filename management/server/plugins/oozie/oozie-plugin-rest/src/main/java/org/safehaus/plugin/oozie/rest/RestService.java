package org.safehaus.plugin.oozie.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


public interface RestService {

    //list clusters
    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String listClusters();

    //view cluster info
    @GET
    @Path( "clusters/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getCluster( @PathParam( "clustername" ) String source );

    //create cluster
    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String createCluster( @QueryParam( "config" ) String config );

    //destroy cluster
    @DELETE
    @Path( "clusters/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyCluster( @PathParam( "clustername" ) String clusterName );

    //start cluster
    @PUT
    @Path( "clusters/{clustername}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String startCluster( @PathParam( "clustername" ) String clusterName );

    //stop cluster
    @PUT
    @Path( "clusters/{clustername}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String stopCluster( @PathParam( "clustername" ) String clusterName );

    //add node
    @POST
    @Path( "clusters/{clustername}/nodes/{lxchostname}/{nodetype}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode( @PathParam( "clustername" ) String clustername,
                           @PathParam( "lxchostname" ) String lxchostname, @PathParam( "nodetype" ) String nodetype );

    //destroy node
    @DELETE
    @Path( "clusters/{clustername}/nodes/{lxchostname}/{nodetype}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode( @PathParam( "clustername" ) String clustername,
                               @PathParam( "lxchostname" ) String lxchostname,
                               @PathParam( "nodetype" ) String nodetype );

    //check node status
    @GET
    @Path( "clusters/{clustername}/nodes/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String checkNode( @PathParam( "clustername" ) String clustername,
                             @PathParam( "lxchostname" ) String lxchostname );
}