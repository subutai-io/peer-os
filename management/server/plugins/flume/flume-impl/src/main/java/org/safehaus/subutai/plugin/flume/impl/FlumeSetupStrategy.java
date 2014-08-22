package org.safehaus.subutai.plugin.flume.impl;

import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
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

}
