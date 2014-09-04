package org.safehaus.plugin.hbase.rest;


import org.safehaus.subutai.plugin.hbase.api.HBase;


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
