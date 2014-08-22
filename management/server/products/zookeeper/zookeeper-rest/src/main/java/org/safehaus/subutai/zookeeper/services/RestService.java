package org.safehaus.subutai.zookeeper.services;


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
    @Path("create_cluster")
    @Produces( { MediaType.APPLICATION_JSON } )
    public String createCluster( @QueryParam("config") String config );

    @GET
    @Path( "destroy_cluster/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyCluster( @PathParam( "clusterName" ) String clusterName );

    @GET
    @Path( "start_node/{clusterName}/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String startNode( @PathParam( "clusterName" ) String clusterName,
                             @PathParam( "lxchostname" ) String lxchostname );

    @GET
    @Path( "stop_node/{clusterName}/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String stopNode( @PathParam( "clusterName" ) String clusterName,
                            @PathParam( "lxchostname" ) String lxchostname );

    @GET
    @Path( "destroy_node/{clusterName}/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "lxchostname" ) String lxchostname );

    @GET
    @Path( "check_node/{clusterName}/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String checkNode( @PathParam( "clusterName" ) String clusterName,
                             @PathParam( "lxchostname" ) String lxchostname );

    @GET
    @Path( "add_node/{clusterName}/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode( @PathParam( "clusterName" ) String clusterName,
                           @PathParam( "lxchostname" ) String lxchostname );

    @GET
    @Path("add_node_standalone/{clusterName}")
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNodeStandalone( @PathParam( "clusterName" ) String clusterName );
}