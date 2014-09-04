package org.safehaus.subutai.plugin.mahout.rest;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.mahout.api.Mahout;


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
        return null;
    }


    @Override
    public String getCluster( final String source ) {
        return null;
    }


    @Override
    public String createCluster( final String config ) {
        return null;
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        return null;
    }


    @Override
    public String startCluster( final String clusterName ) {
        return null;
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
}
