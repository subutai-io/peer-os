package org.safehaus.subutai.plugin.zookeeper.impl;


import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


/**
 * Created by dilshat on 7/25/14.
 */
public class ZookeeperOverHadoopSetupStrategy implements ClusterSetupStrategy {
    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {
        return null;
    }
}
