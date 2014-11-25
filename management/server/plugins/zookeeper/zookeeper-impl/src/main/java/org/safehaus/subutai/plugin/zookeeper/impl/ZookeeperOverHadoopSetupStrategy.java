package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * ZK cluster setup strategy over an existing Hadoop cluster
 */
public class ZookeeperOverHadoopSetupStrategy implements ClusterSetupStrategy
{

    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final TrackerOperation po;
    private final ZookeeperImpl manager;
    private Environment environment;



    public ZookeeperOverHadoopSetupStrategy( final Environment environment,
                                             final ZookeeperClusterConfig zookeeperClusterConfig,
                                             final TrackerOperation po, final ZookeeperImpl zookeeperManager )
    {
        Preconditions.checkNotNull( zookeeperClusterConfig, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( zookeeperManager, "ZK manager is null" );

        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.po = po;
        this.manager = zookeeperManager;
        this.environment = environment;
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException
    {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) ||
                zookeeperClusterConfig.getNodes() == null || zookeeperClusterConfig.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( manager.getCluster( zookeeperClusterConfig.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", zookeeperClusterConfig.getClusterName() ) );
        }

        if ( zookeeperClusterConfig.getSetupType() == SetupType.OVER_HADOOP ) {
            environment = manager.getEnvironmentManager().getEnvironmentByUUID(
                    manager.getHadoopManager().getCluster( zookeeperClusterConfig
                            .getHadoopClusterName() ).getEnvironmentId() );
        }
        Set<ContainerHost> zookeeperNodes = environment.getHostsByIds( zookeeperClusterConfig.getNodes() );
        //check if node agent is connected
        for ( ContainerHost node : zookeeperNodes )
        {
            if ( environment.getContainerHostByHostname( node.getHostname() ) == null )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
            }
        }

        HadoopClusterConfig hadoopClusterConfig =
                manager.getHadoopManager().getCluster( zookeeperClusterConfig.getHadoopClusterName() );
        if ( hadoopClusterConfig == null )
        {
            throw new ClusterSetupException(
                    String.format( "Hadoop cluster %s not found", zookeeperClusterConfig.getHadoopClusterName() ) );
        }

        if ( !hadoopClusterConfig.getAllNodes().containsAll( zookeeperClusterConfig.getNodes() ) )
        {
            throw new ClusterSetupException( String.format( "Not all specified ZK nodes belong to %s Hadoop cluster",
                    hadoopClusterConfig.getClusterName() ) );
        }

        po.addLog( "Checking prerequisites..." );


        //check installed subutai packages
        String checkInstalledCommand =
                manager.getCommands().getCheckInstalledCommand();
        List<CommandResult> commandResultList = runCommandOnContainers( checkInstalledCommand, zookeeperNodes );
        if ( getFailedCommandResults( commandResultList ).size() != 0 )
        {
            throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
        }

        Iterator<ContainerHost> iterator = zookeeperNodes.iterator();
        int nodeIndex = 0;
        while ( iterator.hasNext() )
        {
            ContainerHost host = iterator.next();
            CommandResult result = commandResultList.get( nodeIndex++ );

            if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s already has Zookeeper installed", host.getHostname() ) );
            }
            else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s has no Hadoop installed", host.getHostname() ) );
            }
        }

        po.addLog( String.format( "Installing Zookeeper..." ) );

        //install
        String installCommand = manager.getCommands().getInstallCommand( );
        commandResultList = runCommandOnContainers( installCommand, zookeeperNodes );
        if ( getFailedCommandResults( commandResultList ).size() == 0 )
        {
            po.addLog( "Installation succeeded\nConfiguring cluster..." );


            try
            {
                new ClusterConfiguration( manager, po ).configureCluster( zookeeperClusterConfig, environment );
            }
            catch ( ClusterConfigurationException e )
            {
                throw new ClusterSetupException( e.getMessage() );
            }

            po.addLog( "Saving cluster information to database..." );


            zookeeperClusterConfig.setEnvironmentId( environment.getId() );

            manager.getPluginDAO()
                            .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, zookeeperClusterConfig.getClusterName(),
                                    zookeeperClusterConfig );
            po.addLog( "Cluster information saved to database" );
        }
        else
        {
            StringBuilder stringBuilder = new StringBuilder();
            for ( CommandResult commandResult : getFailedCommandResults( commandResultList ) )
            {
                stringBuilder.append( commandResult.getStdErr() );
            }

            throw new ClusterSetupException(
                    String.format( "Installation failed, %s", stringBuilder ) );
        }

        return zookeeperClusterConfig;
    }


    private List<CommandResult> runCommandOnContainers( String command, final Set<ContainerHost> zookeeperNodes )
    {
        List<CommandResult> commandResults = new ArrayList<>();
        for ( ContainerHost containerHost : zookeeperNodes ) {
            try
            {
                commandResults.add( containerHost.execute( new RequestBuilder( command ).withTimeout( 1800 ) ) );
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }
        return commandResults;
    }



    public List<CommandResult> getFailedCommandResults( final List<CommandResult> commandResultList )
    {
        List<CommandResult> failedCommands = new ArrayList<>();
        for ( CommandResult commandResult : commandResultList ) {
            if ( ! commandResult.hasSucceeded() )
                failedCommands.add( commandResult );
        }
        return failedCommands;
    }
}
