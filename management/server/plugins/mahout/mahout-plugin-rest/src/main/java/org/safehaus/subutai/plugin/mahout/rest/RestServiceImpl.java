package org.safehaus.subutai.plugin.mahout.rest;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.mahout.api.Mahout;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;
import org.safehaus.subutai.plugin.mahout.api.TrimmedMahoutClusterConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class RestServiceImpl implements RestService {

    private Mahout mahoutManager;
    private AgentManager agentManager;


    public Mahout getMahoutManager() {
        return mahoutManager;
    }


    public void setMahoutManager( final Mahout mahoutManager ) {
        this.mahoutManager = mahoutManager;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    @Override
    public String listClusters() {
        List<MahoutClusterConfig> clusters = mahoutManager.getClusters();
        return JsonUtil.toJson( clusters );
    }


    @Override
    public String getCluster( final String source ) {
        MahoutClusterConfig mahoutConfig = mahoutManager.getCluster( source );
        return JsonUtil.toJson( mahoutConfig );
    }


    @Override
    public String createCluster( final String config ) {
        TrimmedMahoutClusterConfig tmcc = JsonUtil.fromJson( config, TrimmedMahoutClusterConfig.class );

        MahoutClusterConfig mahoutConfig = new MahoutClusterConfig();
        mahoutConfig.setClusterName( tmcc.getClusterName() );

        for ( String node : tmcc.getNodes() ) {
            Agent agent = agentManager.getAgentByHostname( node );
            mahoutConfig.getNodes().add( agent );
        }

        UUID uuid = mahoutManager.installCluster( mahoutConfig );

        return wrapUUID( uuid );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        UUID uuid = mahoutManager.uninstallCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String startCluster( final String clusterName ) {
        UUID uuid = mahoutManager.startCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        UUID uuid = mahoutManager.stopCluster( clusterName );
        return wrapUUID( uuid );
    }


    @Override
    public String addNode( final String clustername, final String lxchostname ) {
        UUID uuid = mahoutManager.addNode( clustername, lxchostname );
        return wrapUUID( uuid );
    }


    @Override
    public String destroyNode( final String clustername, final String lxchostname ) {
        UUID uuid = mahoutManager.destroyNode( clustername, lxchostname );
        return wrapUUID( uuid );
    }


    @Override
    public String checkNode( final String clustername, final String lxchostname ) {
        UUID uuid = mahoutManager.checkNode( clustername, lxchostname );
        return wrapUUID( uuid );
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }
}
