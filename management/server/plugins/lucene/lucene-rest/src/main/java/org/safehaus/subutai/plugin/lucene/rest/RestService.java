package org.safehaus.subutai.plugin.lucene.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Lucene luceneManager;

    public void setLuceneManager( Lucene luceneManager )
    {
        this.luceneManager = luceneManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getClusters()
    {

        List<LuceneConfig> configs = luceneManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( LuceneConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        LuceneConfig config = luceneManager.getCluster( clusterName );

        String cluster = JsonUtil.GSON.toJson( config );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters/" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @PathParam( "clusterName" ) String clusterName,
                                    @QueryParam( "hadoopClusterName" ) String hadoopClusterName,
                                    @QueryParam( "nodes" ) String nodes )
    {

        LuceneConfig config = new LuceneConfig();
        config.setClusterName( clusterName );
        config.setHadoopClusterName( hadoopClusterName );


        // BUG: Getting the params as list doesn't work. For example "List<String> nodes". To fix this we get a param
        // as plain string and use splitting.
        for ( String node : nodes.split( "," ) )
        {
            config.getNodes().add( UUID.fromString( node ) );
        }

        UUID uuid = luceneManager.installCluster( config );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = luceneManager.uninstallCluster( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = luceneManager.addNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = luceneManager.uninstallNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
