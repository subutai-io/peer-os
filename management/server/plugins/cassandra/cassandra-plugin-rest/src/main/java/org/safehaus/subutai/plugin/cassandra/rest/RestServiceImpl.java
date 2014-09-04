package org.safehaus.subutai.plugin.cassandra.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.plugin.cassandra.api.TrimmedCassandraConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService {

    private Cassandra cassandraManager;


    @Override
    public String listClusters() {
        List<CassandraConfig> configs = cassandraManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( CassandraConfig config : configs ) {
            clusterNames.add( config.getClusterName() );
        }
        return JsonUtil.toJson( clusterNames );
    }


    @Override
    public String getCluster( final String source ) {
        return JsonUtil.toJson( cassandraManager.getCluster( source ) );
    }


    @Override
    public String createCluster( final String config ) {
        TrimmedCassandraConfig trimmedCassandraConfig = JsonUtil.fromJson( config, TrimmedCassandraConfig.class );

        CassandraConfig cassandraConfig = new CassandraConfig();
        cassandraConfig.setClusterName( trimmedCassandraConfig.getClusterName() );
        cassandraConfig.setDomainName( trimmedCassandraConfig.getDomainName() );
        cassandraConfig.setNumberOfNodes( trimmedCassandraConfig.getNumberOfNodes() );
        cassandraConfig.setNumberOfSeeds( trimmedCassandraConfig.getNumberOfSeeds() );

        UUID uuid = cassandraManager.installCluster( cassandraConfig );
        return wrapUUID( uuid );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        UUID uuid = cassandraManager.uninstallCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String startCluster( final String clusterName ) {
        UUID uuid = cassandraManager.startCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        UUID uuid = cassandraManager.stopCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String addNode( final String clustername, final String lxchostname, final String nodetype ) {
        UUID uuid = cassandraManager.addNode( clustername, lxchostname, nodetype );
        return wrapUUID( uuid );
    }


    @Override
    public String destroyNode( final String clustername, final String lxchostname, final String nodetype ) {
        UUID uuid = cassandraManager.destroyNode( clustername, lxchostname, nodetype );
        return wrapUUID( uuid );
    }


    @Override
    public String checkNode( final String clustername, final String lxchostname ) {
        UUID uuid = cassandraManager.checkNode( clustername, lxchostname );
        return wrapUUID( uuid );
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
