package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class HBaseSetupStrategy implements ClusterSetupStrategy
{

    HBaseClusterConfig config;
    TrackerOperation trackerOperation;
    HBaseImpl manager;


    public HBaseSetupStrategy( final HBaseImpl manager, final TrackerOperation po, final HBaseClusterConfig config )
    {
        this.config = config;
        this.trackerOperation = po;
        this.manager = manager;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        return config;
    }
}
