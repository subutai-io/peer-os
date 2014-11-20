package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperStandaloneSetupStrategy;

import com.google.common.collect.Sets;


/**
 * Adds a node to ZK cluster. Install over a newly created lxc or over an existing hadoop cluster node
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{

    private String lxcHostname;
    private ClusterConfiguration clusterConfiguration;


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
        clusterConfiguration = new ClusterConfiguration( manager, trackerOperation );
    }


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
        clusterConfiguration = new ClusterConfiguration( manager, trackerOperation );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE )
        {
            addStandalone( config );
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            addOverHadoop( config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            addWithHadoop( config );
        }
    }


    private void addOverHadoop( final ZookeeperClusterConfig config )
    {
        trackerOperation.addLogFailed( "Add node functionality is not provided by environment manager now. Aborting!" );
//
//        //check if node agent is connected
//        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
//        if ( lxcAgent == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
//            return;
//        }
//
//        if ( config.getNodes().contains( lxcAgent ) )
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
//            return;
//        }
//
//
//        HadoopClusterConfig hadoopClusterConfig =
//                manager.getHadoopManager().getCluster( config.getHadoopClusterName() );
//
//        if ( hadoopClusterConfig == null )
//        {
//            trackerOperation
//                    .addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
//            return;
//        }
//
//        if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
//                    config.getHadoopClusterName() ) );
//            return;
//        }
//
//        trackerOperation.addLog( "Checking prerequisites..." );
//
//        //check installed subutai packages
//        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
//        manager.getCommandRunner().runCommand( checkInstalledCommand );
//
//        if ( !checkInstalledCommand.hasCompleted() )
//        {
//            trackerOperation.addLogFailed( "Failed to check presence of installed subutai packages" );
//            return;
//        }
//
//        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );
//
//        if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Node %s already has Zookeeper installed", lxcHostname ) );
//            return;
//        }
//        else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
//            return;
//        }
//
//
//        config.getNodes().add( lxcAgent );
//        config.setNumberOfNodes( config.getNumberOfNodes() + 1 );
//
//        trackerOperation.addLog( String.format( "Installing %s...", ZookeeperClusterConfig.PRODUCT_NAME ) );
//
//        //install
//        Command installCommand = manager.getCommands().getInstallCommand( Sets.newHashSet( lxcAgent ) );
//        manager.getCommandRunner().runCommand( installCommand );
//
//        if ( installCommand.hasCompleted() )
//        {
//            trackerOperation.addLog( "Installation succeeded\nReconfiguring cluster..." );
//
//            try
//            {
//                new ClusterConfiguration( manager, trackerOperation ).configureCluster( config );
//            }
//            catch ( ClusterConfigurationException e )
//            {
//                trackerOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
//                return;
//            }
//
//            //update db
//            trackerOperation.addLog( "Updating cluster information in database..." );
//
//            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
//            trackerOperation.addLogDone( "Cluster information updated in database" );
//        }
//        else
//        {
//            trackerOperation.addLogFailed( String.format( "Installation failed, %s\nUse Terminal Module to cleanup",
//                    installCommand.getAllErrors() ) );
//        }
    }


    private void addWithHadoop( final ZookeeperClusterConfig config )
    {

        trackerOperation.addLogFailed( "Add node functionality is not provided by environment manager now. Aborting!" );

//        //check if node agent is connected
//        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
//        if ( lxcAgent == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
//            return;
//        }
//
//        trackerOperation.addLog( "Preparing for node addition..." );
//
//        //check installed subutai packages
//        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
//        manager.getCommandRunner().runCommand( checkInstalledCommand );
//
//        if ( !checkInstalledCommand.hasCompleted() )
//        {
//            trackerOperation.addLogFailed( "Failed to check presence of installed subutai packages" );
//            return;
//        }
//
//        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );
//
//        boolean hasZkInstalled =
//                result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME );
//
//        if ( hasZkInstalled )
//        {
//            trackerOperation.addLog( "Checking prerequisites..." );
//
//            if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) )
//            {
//                trackerOperation.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
//                return;
//            }
//
//            if ( config.getNodes().contains( lxcAgent ) )
//            {
//                trackerOperation.addLogFailed(
//                        String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname,
//                                clusterName ) );
//                return;
//            }
//
//            HadoopClusterConfig hadoopClusterConfig =
//                    manager.getHadoopManager().getCluster( config.getHadoopClusterName() );
//
//            if ( hadoopClusterConfig == null )
//            {
//                trackerOperation
//                        .addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
//                return;
//            }
//
//            if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) )
//            {
//                trackerOperation.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
//                        config.getHadoopClusterName() ) );
//                return;
//            }
//
//            config.getNodes().add( lxcAgent );
//            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );
//
//            try
//            {
//                new ClusterConfiguration( manager, trackerOperation ).configureCluster( config );
//            }
//            catch ( ClusterConfigurationException e )
//            {
//                trackerOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
//                return;
//            }
//
//            //update db
//            trackerOperation.addLog( "Updating cluster information in database..." );
//
//            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
//            trackerOperation.addLogDone( "Cluster information updated in database" );
//        }
//        else
//        {
//            addOverHadoop( config );
//        }
    }


    private void addStandalone( final ZookeeperClusterConfig config )
    {
        trackerOperation.addLogFailed( "Add node functionality is not provided by environment manager now. Aborting!" );

//        try
//        {
//            //create lxc
//            trackerOperation.addLog( "Creating lxc container..." );
//
//            Set<Agent> agents = manager.getContainerManager().clone( config.getTemplateName(), 1, null,
//                    ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy() );
//
//            Agent agent = agents.iterator().next();
//
//            trackerOperation.addLog( "Lxc container created successfully" );
//
//            config.getNodes().add( agent );
//            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );
//
//            //reconfigure cluster
//            try
//            {
//                clusterConfiguration.configureCluster( config );
//            }
//            catch ( ClusterConfigurationException e )
//            {
//                trackerOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
//                return;
//            }
//
//            //update db
//            trackerOperation.addLog( "Updating cluster information in database..." );
//
//            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
//            trackerOperation.addLogDone( "Cluster information updated in database" );
//        }
//        catch ( LxcCreateException ex )
//        {
//            trackerOperation.addLogFailed( ex.getMessage() );
//        }
    }


    public ClusterConfiguration getClusterConfiguration()
    {
        return clusterConfiguration;
    }


    public void setClusterConfiguration( ClusterConfiguration clusterConfiguration )
    {
        this.clusterConfiguration = clusterConfiguration;
    }
}
