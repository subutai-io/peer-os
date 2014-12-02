package org.safehaus.subutai.plugin.presto.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SetupStrategyOverHadoop extends SetupHelper implements ClusterSetupStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupStrategyOverHadoop.class.getName() );
    private Set<UUID> skipInstallation = new HashSet<>();
    private Environment environment;

    public SetupStrategyOverHadoop( TrackerOperation po, PrestoImpl manager, PrestoClusterConfig config, Environment environment )
    {
        super( po, manager, config );
        this.environment = environment;
    }


    @Override
    public PrestoClusterConfig setup() throws ClusterSetupException
    {
        check();
        install();
        return config;
    }


    private void check() throws ClusterSetupException
    {
        po.addLog( "Checking prerequisites..." );

        String m = "Malformed configuration: ";
        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException( m + "Cluster already exists: " + config.getClusterName() );
        }
        if ( config.getCoordinatorNode() == null )
        {
            throw new ClusterSetupException( m + "Coordinator node is not specified" );
        }
        if ( config.getWorkers() == null || config.getWorkers().isEmpty() )
        {
            throw new ClusterSetupException( m + "No workers nodes" );
        }

        checkConnected( environment);

        //check installed packages
        String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        RequestBuilder checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( );
        for( UUID uuid : config.getAllNodes())
        {
            ContainerHost node = environment.getContainerHostById( uuid );
            try
            {
                CommandResult result = node.execute( checkInstalledCommand );
                if(result.getStdOut().contains( Commands.PACKAGE_NAME ))
                {
                    po.addLog( String.format( "Node %s already has Presto installed. Omitting this node from installation",
                            node.getHostname() ) );
                    config.getWorkers().remove( node.getId() );
                    config.getAllNodes().remove( node.getId() );
                }
                /*else if ( !result.getStdOut().contains( hadoopPack ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s has no Hadoop installation", node.getHostname() ) );
                }*/
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }
        if ( config.getWorkers().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation\nInstallation aborted" );
        }
        if ( !config.getAllNodes().contains( config.getCoordinatorNode() ) )
        {
            throw new ClusterSetupException( "Coordinator node was omitted\nInstallation aborted" );
        }

    }


    private void install() throws ClusterSetupException
    {
        try
        {
            po.addLog( "Updating db..." );
            config.setEnvironmentId( environment.getId() );
            manager.getPluginDAO().saveInfo( PrestoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster info saved to DB" );

            //install presto
            po.addLog( "Installing Presto..." );
            for ( ContainerHost node : environment.getContainerHostsByIds( config.getAllNodes() ) )
            {
                CommandResult result = node.execute( manager.getCommands().getInstallCommand() );
                processResult( node, result );
            }
            po.addLog( "Configuring cluster..." );
            configureAsCoordinator( environment.getContainerHostById( config.getCoordinatorNode() ), environment );
            configureAsWorker( environment.getContainerHostsByIds( config.getWorkers() ) );
            startNodes( environment.getContainerHostsByIds( config.getAllNodes() ) );
            po.addLog( "Installation succeeded" );
        } catch ( CommandException e )
        {
            throw new ClusterSetupException(
                    String.format( "Error while installing Presto on container %s; ",
                            e.getMessage() ) );
        }
    }
}
