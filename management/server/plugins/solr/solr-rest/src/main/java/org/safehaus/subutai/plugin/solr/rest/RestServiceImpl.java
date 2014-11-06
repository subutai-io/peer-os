package org.safehaus.subutai.plugin.solr.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;


/**
 * REST implementation of Solr API
 */

public class RestServiceImpl implements RestService
{

    private Solr solrManager;


    public void setSolrManager( Solr solrManager )
    {
        this.solrManager = solrManager;
    }


    @Override
    public Response listClusters()
    {

        List<SolrClusterConfig> configs = solrManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( SolrClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @Override
    public Response getCluster( final String clustername )
    {
        String cluster = JsonUtil.toJson( solrManager.getCluster( clustername ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @Override
    public Response createCluster( final String config )
    {
        TrimmedSolrConfig solrConfig = JsonUtil.fromJson( config, TrimmedSolrConfig.class );
        SolrClusterConfig expandedSolrClusterConfig = new SolrClusterConfig();

        expandedSolrClusterConfig.setClusterName( solrConfig.getClusterName() );
        expandedSolrClusterConfig.setNumberOfNodes( 1 );

        String operationId = wrapUUID( solrManager.installCluster( expandedSolrClusterConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response destroyCluster( final String clusterName )
    {
        String operationId = wrapUUID( solrManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( solrManager.startNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( solrManager.stopNode( clusterName,lxcHostname  ));
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxcHostname)
    {
        String operationId = wrapUUID( solrManager.destroyNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( solrManager.checkNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
