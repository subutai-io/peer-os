package org.safehaus.subutai.plugin.solr.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public String listClusters()
    {

        List<SolrClusterConfig> configs = solrManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( SolrClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        return JsonUtil.toJson( clusterNames );
    }


    @Override
    public String getCluster( final String clustername )
    {
        return JsonUtil.toJson( solrManager.getCluster( clustername ) );
    }


    @Override
    public String createCluster( final String config )
    {
        TrimmedSolrConfig solrConfig = JsonUtil.fromJson( config, TrimmedSolrConfig.class );
        SolrClusterConfig expandedSolrClusterConfig = new SolrClusterConfig();

        expandedSolrClusterConfig.setClusterName( solrConfig.getClusterName() );
        expandedSolrClusterConfig.setNumberOfNodes( 1 );

        return wrapUUID( solrManager.installCluster( expandedSolrClusterConfig ) );
    }


    @Override
    public String destroyCluster( final String clusterName )
    {
        return wrapUUID( solrManager.uninstallCluster( clusterName ) );
    }


    @Override
    public String startNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( solrManager.startNode( clusterName, lxchostname ) );
    }


    @Override
    public String stopNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( solrManager.stopNode( clusterName, lxchostname ) );
    }


    @Override
    public String destroyNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( solrManager.destroyNode( clusterName, lxchostname ) );
    }


    @Override
    public String checkNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( solrManager.checkNode( clusterName, lxchostname ) );
    }
}
