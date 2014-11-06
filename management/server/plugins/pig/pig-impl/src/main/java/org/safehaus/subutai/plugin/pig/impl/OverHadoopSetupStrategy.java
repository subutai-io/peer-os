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
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


class OverHadoopSetupStrategy extends PigSetupStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger( OverHadoopSetupStrategy.class.getName() );
    private Environment environment;

    public OverHadoopSetupStrategy( PigImpl manager, PigConfig config, TrackerOperation po, Environment environment )
    {
        super( manager, config, po );
        this.environment = environment;
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
        for ( Iterator<UUID> it = config.getNodes().iterator(); it.hasNext(); )
        {
            UUID containerUUID = it.next();

            ContainerHost containerHost = environment.getContainerHostByUUID( containerUUID );

            if (  containerHost.getHostname() == null )
            {
                trackerOperation.addLog(
                        String.format( "Node %s is not connected. Omitting this node from installation",
                                containerHost.getHostname() ) );
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

        Map<UUID, CommandResult> hostResult = new HashMap<>();
        for ( ContainerHost host : environment.getContainers() )
        {
                CommandResult result = executeCommand( host, Commands.checkCommand );
                hostResult.put( host.getAgent().getUuid(), result );
                if ( !result.hasSucceeded() )
                {
                    throw new ClusterSetupException( "Failed to check presence of installed packages\nInstallation aborted" );
                }

        }
        for ( Iterator<UUID> it = config.getNodes().iterator(); it.hasNext(); )
        {
            UUID containerUUID = it.next();
            CommandResult result = hostResult.get( containerUUID );
            String hostName = environment.getContainerHostByUUID( containerUUID ).getHostname();

            if ( result.getStdOut().contains( PigConfig.PRODUCT_PACKAGE ) )
            {
                trackerOperation.addLog(
                        String.format( "Node %s already has Pig installed. Omitting this node from installation",
                                hostName ));
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }


        for ( Iterator<UUID> it = config.getNodes().iterator(); it.hasNext(); )
        {
            UUID containerUUID = it.next();
            ContainerHost host = environment.getContainerHostByUUID( containerUUID );
            CommandResult result = executeCommand( host, Commands.installCommand );

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
        return config;
    }

    private CommandResult executeCommand( ContainerHost containerHost, String command )
    {
        CommandResult result = null;
        try
        {
            result = containerHost.execute( new RequestBuilder( command ) );
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not execute command correctly. ", command );
            e.printStackTrace();
        }
        return result;
    }
    private boolean executeCommandAll( Set<UUID> nodes, Environment environment, String command )
    {
        boolean result = true;

        try
        {
            for ( Iterator<UUID> it = nodes.iterator(); it.hasNext(); )
            {
                UUID containerUUID = it.next();
                ContainerHost host = environment.getContainerHostByUUID( containerUUID );
                CommandResult commandResult = host.execute( new RequestBuilder( command ) );
                if( !commandResult.hasSucceeded())
                {
                    result = false;
                    break;
                }
            }
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not execute command correctly. ", command );
            e.printStackTrace();
        }

        return result;
    }

}
