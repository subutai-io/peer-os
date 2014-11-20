package org.safehaus.subutai.plugin.pig.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.pig.api.PigConfig;


class WithHadoopSetupStrategy extends PigSetupStrategy
{

    Environment environment;


    public WithHadoopSetupStrategy( PigImpl manager, PigConfig config, TrackerOperation po )
    {
        super( manager, config, po );
    }


    public void setEnvironment( Environment env )
    {
        this.environment = env;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        try
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


            if ( config.getNodes().isEmpty() )
            {
                throw new ClusterSetupException( "Environment has no nodes with Pig installed" );
            }
            config.getHadoopNodes().clear();
            config.setEnvironmentId( environment.getId() );


            for ( ContainerHost container : environment.getContainers() )
            {
                if ( !container.isConnected() )
                {
                    throw new ClusterSetupException(
                            String.format( "Container %s is not connected", container.getHostname() ) );
                }

                config.getHadoopNodes().add( container.getId() );

                if ( container.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
                {
                    config.getNodes().add( container.getId() );
                }
            }

            if ( config.getNodes().isEmpty() )
            {
                throw new ClusterSetupException( "Environment has no nodes" );
            }

            trackerOperation.addLog( "Saving to db..." );
            manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLog( "Cluster info successfully saved" );

        }
        catch ( PeerException e )
        {
            e.printStackTrace();
        }
        return config;
    }
 }
