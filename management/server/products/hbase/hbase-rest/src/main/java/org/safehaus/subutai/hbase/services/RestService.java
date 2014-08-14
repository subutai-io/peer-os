package org.safehaus.subutai.hbase.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.common.JsonUtil;

import org.apache.commons.lang3.StringUtils;


public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private HBase hbaseManager;


    public void setHbaseManager( HBase hbaseManager ) {
        this.hbaseManager = hbaseManager;
    }


    @GET
    @Path( "getClusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getClusters() {

        List<HBaseConfig> configs = hbaseManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( HBaseConfig config : configs ) {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    @GET
    @Path( "getCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getCluster( @QueryParam( "clusterName" ) String clusterName ) {
        HBaseConfig config = hbaseManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    @GET
    @Path( "startCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String startCluster( @QueryParam( "clusterName" ) String clusterName ) {

        UUID uuid = hbaseManager.startCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "stopCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String stopCluster( @QueryParam( "clusterName" ) String clusterName ) {

        UUID uuid = hbaseManager.stopCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "checkCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String checkCluster( @QueryParam( "clusterName" ) String clusterName ) {

        UUID uuid = hbaseManager.checkCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "uninstallCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String uninstallCluster( @QueryParam( "clusterName" ) String clusterName ) {

        UUID uuid = hbaseManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "installCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String installCluster(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "master" ) String master,
            @QueryParam( "backupMasters" ) String backupMasters,
            @QueryParam( "hadoopNameNode" ) String hadoopNameNode,
            @QueryParam( "nodes" ) List<String> nodes,
            @QueryParam( "quorum" ) List<String> quorum,
            @QueryParam( "region" ) List<String> region
    ) {
        HBaseConfig config = new HBaseConfig();
        config.setClusterName( clusterName );
        config.setMaster( master );
        config.setBackupMasters( backupMasters );
        config.setHadoopNameNode( hadoopNameNode );
        config.getNodes().addAll( nodes );
        config.getQuorum().addAll( quorum );
        config.getRegion().addAll( region );

        UUID uuid = hbaseManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }
}