package org.safehaus.subutai.plugin.accumulo.impl;


import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * Created by dilshat on 7/24/14.
 */
public class AccumuloSetupStrategy implements ClusterSetupStrategy {

    private final ProductOperation po;
    private final AccumuloClusterConfig config;


    public AccumuloSetupStrategy( final ProductOperation po, final AccumuloClusterConfig config ) {
        this.po = po;
        this.config = config;
    }


    /**
     * 1) Setup Zookeeper cluster with Hadoop cluster using ZookeeperSetupStrategy(new strategy using combo ZK+Hadoop
     * Template)
     *
     * 2) Configure Accumulo Cluster using ZookeeperClusterConfig and HadoopClusterConfig
     */
    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return null;
    }
}
