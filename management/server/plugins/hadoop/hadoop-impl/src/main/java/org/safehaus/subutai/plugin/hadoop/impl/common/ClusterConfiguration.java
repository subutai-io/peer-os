package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.logging.Logger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class ClusterConfiguration
{

    private static final Logger LOG = Logger.getLogger( ClusterConfiguration.class.getName() );
    private TrackerOperation po;
    private HadoopImpl hadoopManager;



    public ClusterConfiguration( final TrackerOperation operation, final HadoopImpl cassandraManager)
    {
        this.po = operation;
        this.hadoopManager = cassandraManager;
    }


    public void configureCluster( HadoopClusterConfig config, Environment environment )
            throws ClusterConfigurationException
    {
        Commands commands = new Commands( config );
        po.addLog( String.format( "Configuring cluster: %s", config.getClusterName() ) );

        for ( ContainerHost containerHost : environment.getContainers() )
        {
            po.addLog( "Configuring node: " + containerHost.getId() );
        }

        config.setEnvironmentId( environment.getId() );
        hadoopManager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( "Hadoop cluster data saved into database" );
    }
}
