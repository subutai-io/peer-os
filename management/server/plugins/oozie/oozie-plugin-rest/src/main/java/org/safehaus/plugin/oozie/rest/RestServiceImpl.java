package org.safehaus.plugin.oozie.rest;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
public class RestServiceImpl implements RestService {

    private Oozie oozieManager;
    private Hadoop hadoopManager;


    public Oozie getOozieManager() {
        return oozieManager;
    }


    public void setOozieManager( final Oozie oozieManager ) {
        this.oozieManager = oozieManager;
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    @Override
    public String listClusters() {
        return null;
    }


    @Override
    public String getCluster( final String source ) {
        return null;
    }


    @Override
    public String createCluster( final String config ) {

        TrimmedOozieClusterConfig tocc = JsonUtil.fromJson( config, TrimmedOozieClusterConfig.class );

        HadoopClusterConfig hadoopConfig = hadoopManager.getCluster( tocc.getHadoopClusterName() );
        if ( hadoopConfig == null ) {
            return JsonUtil
                    .toJson( "ERROR", String.format( "Hadoop cluster %s not found", tocc.getHadoopClusterName() ) );
        }

        OozieClusterConfig occ = new OozieClusterConfig();
        occ.setClusterName( tocc.getClusterName() );
        occ.setServer( tocc.getServerHostname() );
        Set<String> clients = new HashSet<>();
        //        Set<String> hadoopNodes = new HashSet<String>();
        for ( Agent agent : hadoopConfig.getAllNodes() ) {
            clients.add( agent.getHostname() );
            //            hadoopNodes.add( agent.getHostname() );
        }
        clients.remove( tocc.getServerHostname() );
        occ.setClients( clients );
        //        config.setHadoopNodes( hadoopNodes );
        occ.setHadoopClusterName( hadoopConfig.getClusterName() );

        UUID uuid = this.oozieManager.installCluster( occ );
        return JsonUtil.toJson( "OPERATION_ID", uuid.toString() );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        UUID uuid = oozieManager.uninstallCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String startCluster( final String clusterName ) {
        OozieClusterConfig occ = oozieManager.getCluster( clusterName );
        UUID uuid = oozieManager.startServer( occ );
        return wrapUUID( uuid );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        return null;
    }


    @Override
    public String addNode( final String clustername, final String lxchostname, final String nodetype ) {
        return null;
    }


    @Override
    public String destroyNode( final String clustername, final String lxchostname, final String nodetype ) {
        return null;
    }


    @Override
    public String checkNode( final String clustername, final String lxchostname ) {
        return null;
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
