package org.safehaus.plugin.oozie.rest;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Oozie oozieManager;
    private Hadoop hadoopManager;
    private static final String OPERATION_ID = "OPERATION_ID";


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    public Oozie getOozieManager() {
        return oozieManager;
    }


    public void setOozieManager( Oozie oozieManager ) {
        this.oozieManager = oozieManager;
    }


    @Override
    public String installCluster( String clusterName, String serverHostname, String hadoopClusterName ) {
        HadoopClusterConfig hadoopHadoopClusterConfig = hadoopManager.getCluster( hadoopClusterName );
        if ( hadoopHadoopClusterConfig == null ) {
            return JsonUtil.toJson( "ERROR", String.format( "Hadoop cluster %s not found", hadoopClusterName ) );
        }

        OozieConfig config = new OozieConfig();
        config.setClusterName( clusterName );
        config.setServer( serverHostname );
        Set<String> clients = new HashSet<>();
        Set<String> hadoopNodes = new HashSet<String>();
        for ( Agent agent : hadoopHadoopClusterConfig.getAllNodes() ) {
            clients.add( agent.getHostname() );
            hadoopNodes.add( agent.getHostname() );
        }
        clients.remove( serverHostname );
        config.setClients( clients );
        config.setHadoopNodes( hadoopNodes );

        UUID uuid = this.oozieManager.installCluster( config );
        return JsonUtil.toJson( OPERATION_ID, uuid.toString() );
    }


    @Override
    public String uninstallCluster( String clusterName ) {
        UUID uuid = oozieManager.uninstallCluster( clusterName );
        return JsonUtil.toJson( OPERATION_ID, uuid.toString() );
    }


    @Override
    public String startCluster( final String clusterName ) {
        OozieConfig config = oozieManager.getCluster( clusterName );
        if ( config == null ) {
            return JsonUtil.toJson( "ERROR", String.format( "Cluster %s not found", clusterName ) );
        }
        oozieManager.startServer( config );
        return JsonUtil.toJson( OPERATION_ID, oozieManager.startServer( config ) );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        OozieConfig config = oozieManager.getCluster( clusterName );
        if ( config == null ) {
            return JsonUtil.toJson( "ERROR", String.format( "Cluster %s not found", clusterName ) );
        }
        oozieManager.stopServer( config );
        return JsonUtil.toJson( OPERATION_ID, oozieManager.startServer( config ) );
    }


    @Override
    public String checkCluster( final String clusterName ) {
        OozieConfig config = oozieManager.getCluster( clusterName );
        if ( config == null ) {
            return JsonUtil.toJson( "ERROR", String.format( "Cluster %s not found", clusterName ) );
        }
        oozieManager.checkServerStatus( config );
        return JsonUtil.toJson( OPERATION_ID, oozieManager.startServer( config ) );
    }
}