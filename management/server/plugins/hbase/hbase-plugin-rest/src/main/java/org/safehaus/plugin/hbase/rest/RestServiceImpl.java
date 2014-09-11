package org.safehaus.plugin.hbase.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;


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
    public Response listClusters() {
        List<HBaseClusterConfig> clusters = hbaseManager.getClusters();
        String clusterNames = JsonUtil.toJson(clusters);
        return Response.status(Response.Status.OK).entity(clusterNames).build();
    }


    @Override
    public Response getCluster(final String source) {
        HBaseClusterConfig cluster = hbaseManager.getCluster( source );
        String clusterName = JsonUtil.toJson(cluster);
        return Response.status(Response.Status.OK).entity(clusterName).build();
    }


    @Override
    public Response createCluster(final String config) {


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

        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response destroyCluster(final String clusterName) {
        UUID uuid = hbaseManager.destroyCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response startCluster(final String clusterName) {
        UUID uuid = hbaseManager.startCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response stopCluster(final String clusterName) {
        UUID uuid = hbaseManager.stopCluster( clusterName );
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response addNode(final String clusterName, final String lxcHostname, final String nodeType) {
        UUID uuid = hbaseManager.addNode(clusterName, lxcHostname, nodeType);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response destroyNode(final String clusterName, final String lxcHostname, final String nodeType) {
        UUID uuid = hbaseManager.destroyNode(clusterName, lxcHostname, nodeType);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response checkNode(final String clusterName, final String lxcHostname) {
        UUID uuid = hbaseManager.checkNode(clusterName, lxcHostname);
        String operationId = wrapUUID(uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
