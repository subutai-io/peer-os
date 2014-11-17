package org.safehaus.subutai.plugin.hipi.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;


class WithHadoopSetupStrategy extends HipiSetupStrategy
{

    public WithHadoopSetupStrategy( HipiImpl manager, HipiConfig config, Environment environment, TrackerOperation po )
    {
        super( manager, config, environment, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        if ( environment == null )
        {
            throw new ClusterSetupException( "Environment not specified" );
        }

        if ( environment.getContainers() == null || environment.getContainers().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        config.setEnvironmentId( environment.getId() );
        config.getNodes().clear();
        config.getHadoopNodes().clear();

        for ( ContainerHost host : environment.getContainers() )
        {
            if ( !host.isConnected() )
            {
                throw new ClusterSetupException( "Node is not connected " + host.getHostname() );
            }

            try
            {
                config.getHadoopNodes().add( host.getId() );
                if ( host.getTemplate().getProducts().contains( CommandFactory.PACKAGE_NAME ) )
                {
                    config.getNodes().add( host.getId() );
                }
            }
            catch ( PeerException ex )
            {
                throw new ClusterSetupException( ex );
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes with Hipi installed" );
        }

        trackerOperation.addLog( "Saving to db ..." );
        boolean saved = manager.getPluginDao().saveInfo( HipiConfig.PRODUCT_KEY, config.getClusterName(), config );
        if ( saved )
        {
            trackerOperation.addLog( "Cluster info successfully saved" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to save installation info" );
        }
        trackerOperation.addLog( "Cluster info successfully saved" );
        return config;
    }
}
