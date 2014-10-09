package org.safehaus.subutai.plugin.oozie.rest;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.TrimmedOozieClusterConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService
{

    private Oozie oozieManager;
    private Hadoop hadoopManager;


    public Oozie getOozieManager()
    {
        return oozieManager;
    }


    public void setOozieManager( final Oozie oozieManager )
    {
        this.oozieManager = oozieManager;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    @Override
    public Response listClusters()
    {
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getCluster( final String clusterName )
    {
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response createCluster( final String config )
    {

        TrimmedOozieClusterConfig tocc = JsonUtil.fromJson( config, TrimmedOozieClusterConfig.class );

        HadoopClusterConfig hadoopConfig = hadoopManager.getCluster( tocc.getHadoopClusterName() );
        if ( hadoopConfig == null )
        {

            String errorMsg = JsonUtil.toJson( "ERROR",
                    String.format( "Hadoop cluster %s not found", tocc.getHadoopClusterName() ) );
            return Response.status( Response.Status.UNSUPPORTED_MEDIA_TYPE ).entity( errorMsg ).build();
        }

        OozieClusterConfig occ = new OozieClusterConfig();
        occ.setClusterName( tocc.getClusterName() );
        occ.setServer( tocc.getServerHostname() );
        Set<String> clients = new HashSet<>();
        //        Set<String> hadoopNodes = new HashSet<String>();
        for ( Agent agent : hadoopConfig.getAllNodes() )
        {
            clients.add( agent.getHostname() );
            //            hadoopNodes.add( agent.getHostname() );
        }
        clients.remove( tocc.getServerHostname() );
        occ.setClients( clients );
        //        config.setHadoopNodes( hadoopNodes );
        occ.setHadoopClusterName( hadoopConfig.getClusterName() );

        UUID uuid = this.oozieManager.installCluster( occ );
        String operationId = JsonUtil.toJson( "OPERATION_ID", uuid.toString() );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response destroyCluster( final String clusterName )
    {
        UUID uuid = oozieManager.uninstallCluster( clusterName );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startCluster( final String clusterName )
    {
        OozieClusterConfig occ = oozieManager.getCluster( clusterName );
        UUID uuid = oozieManager.startServer( occ );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopCluster( final String clusterName )
    {
        OozieClusterConfig occ = oozieManager.getCluster( clusterName );
        UUID uuid = oozieManager.stopServer( occ );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNode( final String clusterName, final String lxcHostname, final String nodeType )
    {
        UUID uuid = oozieManager.addNode( clusterName, lxcHostname, nodeType );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxcHostname )
    {
        UUID uuid = oozieManager.destroyNode( clusterName, lxcHostname );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxcHostname )
    {
        OozieClusterConfig occ = oozieManager.getCluster( clusterName );
        UUID uuid = oozieManager.checkServerStatus( occ );
        String operationId = wrapUUID( uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
