package org.safehaus.plugin.hbase.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;

import com.google.common.base.Strings;


//@Path("hbase")
public class RestServiceImplOld {

    private static final String OPERATION_ID = "OPERATION_ID";

    private HBase hbaseManager;


    public void setHbaseManager( HBase hbaseManager ) {
        this.hbaseManager = hbaseManager;
    }


    public String getClusters() {

        List<HBaseClusterConfig> configs = hbaseManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( HBaseClusterConfig config : configs ) {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    public String getCluster(  String clusterName ) {
        HBaseClusterConfig config = hbaseManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    public String startCluster(  String clusterName ) {

        UUID uuid = hbaseManager.startCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    public String stopCluster(  String clusterName ) {

        UUID uuid = hbaseManager.stopCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    public String checkCluster(  String clusterName ) {

        UUID uuid = hbaseManager.checkCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    public String uninstallCluster(  String clusterName ) {

        UUID uuid = hbaseManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    public String installCluster(  String clusterName,  String master,
                                   String backupMasters,
                                   String hadoopNameNode,
                                   String nodes,  String quorum,
                                   String region ) {
        HBaseClusterConfig config = new HBaseClusterConfig();
        config.setClusterName( clusterName );
        config.setMaster( master );
        config.setBackupMasters( backupMasters );
        config.setHadoopNameNode( hadoopNameNode );

        // BUG: Getting the params as list doesn't work. For example "List<String> nodes". To fix this we get a param
        // as plain string and use splitting.
        if ( !Strings.isNullOrEmpty( nodes ) ) {
            for ( String node : nodes.split( "," ) ) {
                config.getNodes().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( quorum ) ) {
            for ( String node : quorum.split( "," ) ) {
                config.getQuorum().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( region ) ) {
            for ( String node : region.split( "," ) ) {
                config.getRegion().add( node );
            }
        }

        UUID uuid = hbaseManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }
}