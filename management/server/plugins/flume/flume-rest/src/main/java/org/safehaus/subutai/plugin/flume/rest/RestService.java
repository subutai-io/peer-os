package org.safehaus.subutai.plugin.flume.rest;


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

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Flume flumeManager;

    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setFlumeManager( Flume flumeManager )
    {
        this.flumeManager = flumeManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getClusters()
    {

        List<FlumeConfig> configs = flumeManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( FlumeConfig config : configs )
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
        FlumeConfig config = flumeManager.getCluster( clusterName );

        String cluster = JsonUtil.GSON.toJson( config );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @PathParam( "clusterName" ) String clusterName,
                                    @QueryParam( "nodes" ) String nodes )
    {

        FlumeConfig config = new FlumeConfig();
        config.setSetupType( SetupType.OVER_HADOOP );
        config.setClusterName( clusterName );

        String[] arr = nodes.split( "[,;]" );
        for ( String node : arr )
        {
            if ( UUID.fromString( node ) != null )
            {
                config.getNodes().add( UUID.fromString( node ) );
            }
        }

        UUID uuid = flumeManager.installCluster( config );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @POST
    @Path( "install/{name}/{hadoopName}/{slaveNodesCount}/{replFactor}/{domainName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response install( @PathParam( "name" ) String name, @PathParam( "hadoopName" ) String hadoopName,
                             @PathParam( "slaveNodesCount" ) String slaveNodesCount,
                             @PathParam( "replFactor" ) String replFactor,
                             @PathParam( "domainName" ) String domainName )
    {

        FlumeConfig config = new FlumeConfig();
        config.setClusterName( name );
        config.setHadoopClusterName( hadoopName );
        config.setSetupType( SetupType.WITH_HADOOP );

        HadoopClusterConfig hc = new HadoopClusterConfig();
        hc.setClusterName( hadoopName );
        if ( domainName != null )
        {
            hc.setDomainName( domainName );
        }
        try
        {
            int i = Integer.parseInt( slaveNodesCount );
            hc.setCountOfSlaveNodes( i );
        }
        catch ( NumberFormatException ex )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( ex.getMessage() ).build();
        }
        try
        {
            int i = Integer.parseInt( replFactor );
            hc.setReplicationFactor( i );
        }
        catch ( NumberFormatException ex )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( ex.getMessage() ).build();
        }

        UUID trackId = flumeManager.installCluster( config, hc );

        String operationId = JsonUtil.toJson( OPERATION_ID, trackId );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = flumeManager.uninstallCluster( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = flumeManager.addNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{node}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = flumeManager.destroyNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{node}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = flumeManager.startNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{node}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = flumeManager.stopNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path( "clusters/{clusterName}/nodes/{node}/check" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response checkNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "node" ) String node )
    {
        UUID uuid = flumeManager.checkNode( clusterName, node );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
