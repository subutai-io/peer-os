package org.safehaus.subutai.plugin.pig.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


class OverHadoopSetupStrategy extends PigSetupStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger( OverHadoopSetupStrategy.class.getName() );

    public OverHadoopSetupStrategy( PigImpl manager, PigConfig config, TrackerOperation po )
    {
        super( manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) || CollectionUtil
                .isCollectionEmpty( config.getNodes() ) )
        {
            throw new ClusterSetupException( "Malformed configuration\nInstallation aborted" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
        }

        // Check if node agent is connected
        for ( Iterator<ContainerHost> it = config.getNodes().iterator(); it.hasNext(); )
        {
            ContainerHost host = it.next();

            if (  host.getHostname() == null )
            {
                trackerOperation.addLog(
                        String.format( "Node %s is not connected. Omitting this node from installation",
                                host.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        // Check installed packages
        trackerOperation.addLog( "Installing Pig..." );

        Map<ContainerHost, CommandResult> hostResult = new HashMap<>();
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        for ( ContainerHost host : environment.getContainers() )
        {
            try
            {
                CommandResult result = host.execute( new RequestBuilder( Commands.checkCommand ) );
                hostResult.put( host, result );
                if ( !result.hasSucceeded() )
                {
                    throw new ClusterSetupException( "Failed to check presence of installed packages\nInstallation aborted" );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
        for ( Iterator<ContainerHost> it = config.getNodes().iterator(); it.hasNext(); )
        {
            ContainerHost host = it.next();
            CommandResult result = hostResult.get( host );

            if ( result.getStdOut().contains( PigConfig.PRODUCT_PACKAGE ) )
            {
                trackerOperation.addLog(
                        String.format( "Node %s already has Pig installed. Omitting this node from installation",
                                host.getHostname() ));
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        for ( Iterator<ContainerHost> it = config.getNodes().iterator(); it.hasNext(); )
        {
            ContainerHost host = it.next();
            try
            {
                CommandResult result = host.execute( new RequestBuilder( Commands.installCommand ) );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( "Installation succeeded" );
                    trackerOperation.addLog( "Updating db..." );
                    config.setEnvironmentId( environment.getId() );
                    manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, config.getClusterName(), config );
                }
                else
                {
                    trackerOperation
                            .addLogFailed( String.format( "Installation failed, %s", result.getStdErr() ) );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }

        return config;
    }
}
