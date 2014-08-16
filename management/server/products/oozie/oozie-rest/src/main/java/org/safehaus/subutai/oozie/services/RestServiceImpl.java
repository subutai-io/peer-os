package org.safehaus.subutai.oozie.services;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;


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
        Config hadoopConfig = hadoopManager.getCluster( hadoopClusterName );
        if ( hadoopConfig == null ) {
            return JsonUtil.toJson( "ERROR", String.format( "Hadoop cluster %s not found", hadoopClusterName ) );
        }

        OozieConfig config = new OozieConfig();
        config.setClusterName( clusterName );
        config.setServer( serverHostname );
        Set<String> clients = new HashSet<String>();

        for ( Agent agent : hadoopConfig.getAllNodes() ) {
            clients.add( agent.getHostname() );
        }
        clients.remove( serverHostname );
        config.setClients( clients );

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