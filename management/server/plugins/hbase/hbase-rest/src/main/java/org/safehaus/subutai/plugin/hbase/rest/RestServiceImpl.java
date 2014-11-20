package org.safehaus.subutai.plugin.hbase.rest;


import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService
{

    private HBase hbaseManager;


    public HBase getHbaseManager()
    {
        return hbaseManager;
    }


    public void setHbaseManager( final HBase hbaseManager )
    {
        this.hbaseManager = hbaseManager;
    }


    @Override
    public Response listClusters()
    {
        List<HBaseConfig> clusters = hbaseManager.getClusters();
        String clusterNames = JsonUtil.toJson( clusters );
        return Response.status( Response.Status.OK ).entity( clusterNames ).build();
    }


    @Override
    public Response getCluster( final String source )
    {
        HBaseConfig cluster = hbaseManager.getCluster( source );
        String clusterName = JsonUtil.toJson( cluster );
        return Response.status( Response.Status.OK ).entity( clusterName ).build();
    }


    @Override
    public Response createCluster( final String config )
    {


        HBaseConfig hbcc = new HBaseConfig();
        /*hbcc.setClusterName( clusterName );
        hbcc.setHbaseMaster( master );
        hbcc.setBackupMasters( backupMasters );
        hbcc.setHadoopNameNode( hadoopNameNode );


        if ( !Strings.isNullOrEmpty( nodes ) ) {
            for ( String node : nodes.split( "," ) ) {
                hbcc.getHadoopNodes().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( quorum ) ) {
            for ( String node : quorum.split( "," ) ) {
                hbcc.getQuorumPeers().add( node );
            }
        }

        if ( !Strings.isNullOrEmpty( region ) ) {
            for ( String node : region.split( "," ) ) {
                hbcc.getRegionServers().add( node );
            }
        }*/

        UUID uuid = hbaseManager.installCluster( hbcc );

        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response destroyCluster( final String clusterName )
    {
        UUID uuid = hbaseManager.uninstallCluster( clusterName );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startCluster( final String clusterName )
    {
        UUID uuid = hbaseManager.startCluster( clusterName );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopCluster( final String clusterName )
    {
        UUID uuid = hbaseManager.stopCluster( clusterName );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String containerId, final String nodeType )
    {
        UUID uuid = hbaseManager.destroyNode( clusterName, containerId );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String containerId )
    {
        UUID uuid = hbaseManager.checkNode( clusterName, UUID.fromString( containerId ) );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response addNode( final String clusterName, final String nodeType )
    {
        UUID uuid = hbaseManager.addNode( clusterName, nodeType );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
