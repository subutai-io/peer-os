package org.safehaus.plugin.hbase.rest;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService {

    private HBase hbaseManager;


    public HBase getHbaseManager() {
        return hbaseManager;
    }


    public void setHbaseManager( final HBase hbaseManager ) {
        this.hbaseManager = hbaseManager;
    }


    @Override
    public String listClusters() {
        List<HBaseClusterConfig> clusters = hbaseManager.getClusters();
        return JsonUtil.toJson( clusters );
    }


    @Override
    public String getCluster( final String source ) {
        HBaseClusterConfig cluster = hbaseManager.getCluster( source );
        return JsonUtil.toJson( cluster );
    }


    @Override
    public String createCluster( final String config ) {


        HBaseClusterConfig hbcc = new HBaseClusterConfig();
        /*hbcc.setClusterName( clusterName );
        hbcc.setMaster( master );
        hbcc.setBackupMasters( backupMasters );
        hbcc.setHadoopNameNode( hadoopNameNode );


        if ( !Strings.isNullOrEmpty( nodes ) ) {
            for ( String node : nodes.split( "," ) ) {
                hbcc.getNodes().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( quorum ) ) {
            for ( String node : quorum.split( "," ) ) {
                hbcc.getQuorum().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( region ) ) {
            for ( String node : region.split( "," ) ) {
                hbcc.getRegion().add( node );
            }
        }*/

        UUID uuid = hbaseManager.installCluster( hbcc );

        return wrapUUID( uuid );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        UUID uuid = hbaseManager.destroyCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String startCluster( final String clusterName ) {
        UUID uuid = hbaseManager.startCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        UUID uuid = hbaseManager.stopCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String addNode( final String clustername, final String lxchostname, final String nodetype ) {
        UUID uuid = hbaseManager.addNode( clustername, lxchostname, nodetype );
        return wrapUUID( uuid );
    }


    @Override
    public String destroyNode( final String clustername, final String lxchostname, final String nodetype ) {
        UUID uuid = hbaseManager.destroyNode( clustername, lxchostname, nodetype );
        return wrapUUID( uuid );
    }


    @Override
    public String checkNode( final String clustername, final String lxchostname ) {
        UUID uuid = hbaseManager.checkNode( clustername, lxchostname );
        return wrapUUID( uuid );
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
