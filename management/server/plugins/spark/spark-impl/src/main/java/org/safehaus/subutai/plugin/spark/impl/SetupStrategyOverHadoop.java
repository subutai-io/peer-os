package org.safehaus.subutai.plugin.spark.impl;


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class SetupStrategyOverHadoop implements ClusterSetupStrategy
{
    final TrackerOperation po;
    final SparkImpl manager;
    final SparkClusterConfig config;
    private Environment environment;
    private Set<ContainerHost> nodesToInstallSpark;


    public SetupStrategyOverHadoop( TrackerOperation po, SparkImpl manager, SparkClusterConfig config,
                                    Environment environment )
    {
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( manager, "Manager is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );


        this.po = po;
        this.manager = manager;
        this.config = config;
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        check();
        configure();
        return config;
    }


    private void check() throws ClusterSetupException
    {

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException( String.format( "Cluster %s already exists", config.getClusterName() ) );
        }
        if ( config.getMasterNodeId() == null )
        {
            throw new ClusterSetupException( "Master node not specified" );
        }
        if ( CollectionUtil.isCollectionEmpty( config.getSlaveIds() ) )
        {
            throw new ClusterSetupException( "No slave nodes" );
        }

        ContainerHost master = environment.getContainerHostById( config.getMasterNodeId() );
        if ( master == null )
        {
            throw new ClusterSetupException( "Master not found in the environment" );
        }
        if ( !master.isConnected() )
        {
            throw new ClusterSetupException( "Master is not connected" );
        }

        Set<ContainerHost> slaves = environment.getContainerHostsByIds( config.getSlaveIds() );

        if ( slaves.size() > config.getSlaveIds().size() )
        {
            throw new ClusterSetupException( "Fewer slaves found in the environment than indicated" );
        }

        for ( ContainerHost slave : slaves )
        {
            if ( !slave.isConnected() )
            {
                throw new ClusterSetupException(
                        String.format( "Container %s is not connected", slave.getHostname() ) );
            }
        }

        // check Hadoop cluster
        HadoopClusterConfig hc = manager.hadoopManager.getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }
        if ( !hc.getAllNodes().containsAll( config.getAllNodesIds() ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }

        po.addLog( "Checking prerequisites..." );

        //gather all nodes
        final Set<ContainerHost> allNodes = Sets.newHashSet( master );
        allNodes.addAll( slaves );

        //check if node belongs to some existing spark cluster
        List<SparkClusterConfig> sparkClusters = manager.getClusters();
        for ( ContainerHost node : allNodes )
        {
            for ( SparkClusterConfig cluster : sparkClusters )
            {
                if ( cluster.getAllNodesIds().contains( node.getId() ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s already belongs to Spark cluster %s", node.getHostname(),
                                    cluster.getClusterName() ) );
                }
            }
        }

        nodesToInstallSpark = Sets.newHashSet();

        //check hadoop installation & filter nodes needing Spark installation
        RequestBuilder checkInstalledCommand = manager.getCommands().getCheckInstalledCommand();

        for ( Iterator<ContainerHost> iterator = allNodes.iterator(); iterator.hasNext(); )
        {
            final ContainerHost node = iterator.next();
            try
            {
                CommandResult result = node.execute( checkInstalledCommand );
                if ( !result.getStdOut().contains( Commands.PACKAGE_NAME ) )
                {
                    nodesToInstallSpark.add( node );
                }
                if ( !result.getStdOut()
                            .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME.toLowerCase() ) )
                {
                    po.addLog(
                            String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                                    node.getHostname() ) );
                    config.getSlaveIds().remove( node.getId() );
                    iterator.remove();
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
            }
        }

        if ( config.getSlaveIds().isEmpty() )
        {
            throw new ClusterSetupException( "No slave nodes eligible for installation" );
        }
        if ( !allNodes.contains( master ) )
        {
            throw new ClusterSetupException( "Master node was omitted" );
        }
    }


    private void configure() throws ClusterSetupException
    {

        if ( !nodesToInstallSpark.isEmpty() )
        {
            po.addLog( "Installing Spark..." );
            //install spark
            RequestBuilder installCommand = manager.getCommands().getInstallCommand();
            for ( ContainerHost node : nodesToInstallSpark )
            {
                executeCommand( node, installCommand );
            }
        }

        po.addLog( "Configuring cluster..." );

        ClusterConfiguration configuration = new ClusterConfiguration( manager, po );

        try
        {
            configuration.configureCluster( config, environment );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e );
        }

        po.addLog( "Saving cluster info..." );

        config.setEnvironmentId( environment.getId() );

        try
        {
            manager.saveConfig( config );
        }
        catch ( ClusterException e )
        {
            throw new ClusterSetupException( e );
        }
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterSetupException
    {

        CommandResult result;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            throw new ClusterSetupException( e );
        }
        if ( !result.hasSucceeded() )
        {
            throw new ClusterSetupException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
        return result;
    }
}
