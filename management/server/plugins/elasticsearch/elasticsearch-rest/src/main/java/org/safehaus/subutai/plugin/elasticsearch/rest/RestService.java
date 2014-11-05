package org.safehaus.subutai.plugin.elasticsearch.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";
    private Elasticsearch elasticsearch;


    public void setElasticsearch( final Elasticsearch elasticsearch )
    {
        this.elasticsearch = elasticsearch;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listClusters()
    {
        List<ElasticsearchClusterConfiguration> elasticsearchClusterConfigurationList = elasticsearch.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( ElasticsearchClusterConfiguration elasticsearchClusterConfiguration :
                elasticsearchClusterConfigurationList )
        {
            clusterNames.add( elasticsearchClusterConfiguration.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @PathParam( "clusterName" ) String clusterName,
                                    @QueryParam( "numberOfNodes" ) int numberOfNodes )
    {

        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = new ElasticsearchClusterConfiguration();
        elasticsearchClusterConfiguration.setClusterName( clusterName );
        elasticsearchClusterConfiguration.setNumberOfNodes( numberOfNodes );

        UUID uuid = elasticsearch.installCluster( elasticsearchClusterConfiguration );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = elasticsearch.uninstallCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path( "clusters/{clusterName}/nodes" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response checkAllNodes( @PathParam( "clusterName" ) String clusterName )
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster( clusterName );
        UUID uuid = elasticsearch.checkAllNodes( config );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startAllNodes( @PathParam( "clusterName" ) String clusterName )
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster( clusterName );
        UUID uuid = elasticsearch.startAllNodes( config );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopAllNodes( @PathParam( "clusterName" ) String clusterName )
    {
        ElasticsearchClusterConfiguration config = elasticsearch.getCluster( clusterName );
        UUID uuid = elasticsearch.stopAllNodes( config );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = elasticsearch.addNode( clusterName, node );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = elasticsearch.destroyNode( clusterName, node );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}