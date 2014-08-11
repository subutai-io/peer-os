package org.safehaus.subutai.accumulo.services;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


public interface RestService {

    @GET
    @Path( "list_clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String listClusters();

    @GET
    @Path( "get_cluster/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getCluster( @PathParam( "clustername" ) String source );

    @GET
    @Path( "destroy_cluster/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyCluster( @PathParam( "clustername" ) String clusterName );

    @GET
    @Path( "start_cluster/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String startCluster( @PathParam( "clustername" ) String clusterName );

    @GET
    @Path( "stop_cluster/{clustername}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String stopCluster( @PathParam( "clustername" ) String clusterName );

    @GET
    @Path( "create_cluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String createCluster( @QueryParam( "config" ) String config );

    @GET
    @Path( "add_node" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode( @QueryParam( "clustername" ) String clustername,
                           @QueryParam( "lxchostname" ) String lxchostname, @QueryParam( "nodetype" ) String nodetype );

    @GET
    @Path( "destroy_node" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode( @QueryParam( "clustername" ) String clustername,
                               @QueryParam( "lxchostname" ) String lxchostname,
                               @QueryParam( "nodetype" ) String nodetype );
}