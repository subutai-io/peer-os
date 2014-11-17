package org.safehaus.subutai.plugin.shark.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class SetupStrategyOverSpark implements ClusterSetupStrategy
{

    private final Environment environment;
    private final SharkImpl manager;
    private final SharkClusterConfig config;
    private final TrackerOperation trackerOperation;

    private SparkClusterConfig sparkConfig;
    private ContainerHost sparkMaster;
    private List<ContainerHost> allNodes;


    public SetupStrategyOverSpark( Environment environment, SharkImpl manager, SharkClusterConfig config,
                                   TrackerOperation trackerOperation )
    {

        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( manager );
        Preconditions.checkNotNull( config );
        Preconditions.checkNotNull( trackerOperation );

        this.environment = environment;
        this.manager = manager;
        this.config = config;
        this.trackerOperation = trackerOperation;
    }


    private void check() throws ClusterSetupException
    {

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException( String.format( "Cluster %s already exists", config.getClusterName() ) );
        }

        sparkConfig = manager.getSparkManager().getCluster( config.getSparkClusterName() );
        if ( sparkConfig == null )
        {
            throw new ClusterSetupException(
                    String.format( "Underlying Spark cluster '%s' not found.", config.getSparkClusterName() ) );
        }

        final Set<ContainerHost> sparkSlaves =
                environment.getHostsByIds( Sets.newHashSet( sparkConfig.getSlaveIds() ) );

        if ( CollectionUtil.isCollectionEmpty( sparkSlaves ) )
        {
            throw new ClusterSetupException( "Spark slave nodes not found" );
        }

        if ( sparkSlaves.size() < sparkConfig.getSlaveIds().size() )
        {
            throw new ClusterSetupException( "Fewer Spark nodes found in environment than exist in Spark cluster" );
        }

        for ( ContainerHost slave : sparkSlaves )
        {
            if ( !slave.isConnected() )
            {
                throw new ClusterSetupException(
                        String.format( "Container %s is not connected", slave.getHostname() ) );
            }
        }

        sparkMaster = environment.getContainerHostByUUID( sparkConfig.getMasterNodeId() );

        if ( sparkMaster == null )
        {
            throw new ClusterSetupException( "Spark master not found" );
        }

        if ( !sparkMaster.isConnected() )
        {
            throw new ClusterSetupException( "Spark master is not connected" );
        }

        trackerOperation.addLog( "Checking installed packages..." );
        allNodes = Lists.newArrayList( sparkSlaves );
        allNodes.add( sparkMaster );

        RequestBuilder checkCommand = manager.getCommands().getCheckInstalledCommand();

        for ( ContainerHost node : allNodes )
        {
            try
            {
                CommandResult result = executeCommand( node, checkCommand );
                if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s already has Shark installed", node.getHostname() ) );
                }
            }
            catch ( ClusterException e )
            {
                throw new ClusterSetupException( e );
            }
        }
    }


    private void configure() throws ClusterSetupException
    {
        config.getNodeIds().clear();
        config.getNodeIds().addAll( sparkConfig.getAllNodesIds() );
        config.setEnvironmentId( environment.getId() );

        trackerOperation.addLog( "Setting master IP..." );

        RequestBuilder setMasterIpCommand = manager.getCommands().getSetMasterIPCommand( sparkMaster );
        for ( ContainerHost node : allNodes )
        {
            try
            {
                executeCommand( node, setMasterIpCommand );
            }
            catch ( ClusterException e )
            {
                throw new ClusterSetupException( e );
            }
        }

        trackerOperation.addLog( "Updating db..." );

        if ( !manager.getPluginDao().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            throw new ClusterSetupException( "Could not save cluster info" );
        }
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        check();

        configure();


        return config;
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterException
    {

        return executeCommand( host, command, false );
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command, boolean skipError )
            throws ClusterException
    {

        CommandResult result = null;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            if ( skipError )
            {
                trackerOperation
                        .addLog( String.format( "Error on container %s: %s", host.getHostname(), e.getMessage() ) );
            }
            else
            {
                throw new ClusterException( e );
            }
        }
        if ( skipError )
        {
            if ( result != null && !result.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        else
        {
            if ( !result.hasSucceeded() )
            {
                throw new ClusterException( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        return result;
    }
}

