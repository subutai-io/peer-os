package org.safehaus.subutai.plugin.zookeeper.impl;


import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


/**
 * This is a zk cluster setup strategy.
 */
public class ZookeeperSetupStrategy implements ClusterSetupStrategy {
    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {
        return null;
    }
}
