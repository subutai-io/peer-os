package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;

import com.google.common.base.Preconditions;


abstract class HBaseSetupStrategy implements ClusterSetupStrategy
{

    final Environment environment;
    final HBaseImpl manager;
    final HBaseConfig config;
    final TrackerOperation trackerOperation;


    public HBaseSetupStrategy( HBaseImpl manager, HBaseConfig config, Environment environment,
                               TrackerOperation operation )
    {
        Preconditions.checkNotNull( manager );
        Preconditions.checkNotNull( config );
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( operation );
        this.manager = manager;
        this.config = config;
        this.trackerOperation = operation;
        this.environment = environment;
    }


    void checkConfig() throws ClusterSetupException
    {
        String m = "Invalid configuration: ";

        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "Cluster name not specified" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "Cluster '%s' already exists", config.getClusterName() ) );
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            if ( config.getAllNodes() == null || config.getAllNodes().isEmpty() )
            {
                throw new ClusterSetupException( m + "Target nodes not specified" );
            }
        }
    }
}
