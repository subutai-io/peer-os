package org.safehaus.subutai.plugin.flume.impl;

import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

abstract class FlumeSetupStrategy implements ClusterSetupStrategy {

    final FlumeImpl manager;
    final FlumeConfig config;
    final ProductOperation po;

    public FlumeSetupStrategy(FlumeImpl manager, FlumeConfig config, ProductOperation po) {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }

    void checkConfig() throws ClusterSetupException {
        String m = "Invalid configuration: ";

        if(config.getClusterName() == null || config.getClusterName().isEmpty())
            throw new ClusterSetupException(m + "Cluster name not specified");

        if(manager.getCluster(config.getClusterName()) != null)
            throw new ClusterSetupException(m + String.format(
                    "Cluster '%s' already exists", config.getClusterName()));

        if(config.getSetupType() == SetupType.OVER_HADOOP)
            if(config.getNodes() == null || config.getNodes().isEmpty())
                throw new ClusterSetupException(m + "Target nodes not specified");
    }
}
