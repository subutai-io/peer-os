package org.safehaus.subutai.plugin.cassandra.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


//@Path("cassandra")
public interface RestService {

    @GET
    @Path("install/{clusterName}/{domainName}/{numberOfNodes}/{numberOfSeeds}")
    @Produces(MediaType.APPLICATION_JSON)
    public String install( @PathParam("clusterName") String clusterName, @PathParam("domainName") String domainName,
                           @PathParam("numberOfNodes") String numberOfNodes,
                           @PathParam("numberOfSeeds") String numberOfSeeds );

    @GET
    @Path("uninstall/{clusterName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String uninstall( @PathParam("clusterName") String clusterName );

    //    @POST
    //    @Path("install_from_json")
    //    @Consumes(MediaType.APPLICATION_JSON)
    //    public Response installFromJson( final String json );

    @GET
    @Path("startNode/{clusterName}/{lxchostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String startNode( @PathParam("clusterName") String clusterName,
                             @PathParam("lxchostname") String lxchostname );

    @GET
    @Path("stopNode/{clusterName}/{lxchostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String stopNode( @PathParam("clusterName") String clusterName,
                            @PathParam("lxchostname") String lxchostname );

    /*@GET
    @Path("checkNode/{clusterName}/{lxchostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkNode( @PathParam("clusterName") String clusterName,
                             @PathParam("lxchostname") String lxchostname );*/


    // === new REST calls

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