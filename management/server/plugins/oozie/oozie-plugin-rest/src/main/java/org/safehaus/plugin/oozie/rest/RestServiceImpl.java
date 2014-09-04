package org.safehaus.plugin.oozie.rest;


import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;


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
