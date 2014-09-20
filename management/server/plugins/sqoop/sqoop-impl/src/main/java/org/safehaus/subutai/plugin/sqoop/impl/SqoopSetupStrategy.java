package org.safehaus.subutai.plugin.sqoop.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;


abstract class SqoopSetupStrategy implements ClusterSetupStrategy
{

    final SqoopImpl manager;
    final SqoopConfig config;
    final ProductOperation po;


    public SqoopSetupStrategy( SqoopImpl manager, SqoopConfig config, ProductOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


    public void checkConfig() throws ClusterSetupException
    {

        String m = "Invalid configuration: ";

        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "name is not specified" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "Sqoop installation already exists: %s", config.getClusterName() ) );
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            if ( config.getNodes() == null || config.getNodes().isEmpty() )
            {
                throw new ClusterSetupException( m + "Target nodes not specified" );
            }
        }
    }
}
