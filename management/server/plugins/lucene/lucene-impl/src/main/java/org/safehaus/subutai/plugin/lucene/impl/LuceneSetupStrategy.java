package org.safehaus.subutai.plugin.lucene.impl;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.lucene.api.*;


abstract class LuceneSetupStrategy implements ClusterSetupStrategy
{

    final LuceneImpl manager;
    final Config config;
    final ProductOperation po;

    public LuceneSetupStrategy( LuceneImpl manager, Config config, ProductOperation po ) {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }

    void checkConfig() throws ClusterSetupException
    {
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
