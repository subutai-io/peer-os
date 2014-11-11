//package org.safehaus.subutai.plugin.accumulo.impl.handler;
//
//
//import java.util.UUID;
//
//import org.safehaus.subutai.common.exception.ClusterConfigurationException;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.settings.Common;
//import org.safehaus.subutai.core.command.api.command.AgentResult;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
//import org.safehaus.subutai.plugin.accumulo.api.NodeType;
//import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
//import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;
//import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
//import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
//
//import com.google.common.base.Preconditions;
//import com.google.common.base.Strings;
//import com.google.common.collect.Sets;
//
//
///**
// * Handles add note operation
// */
//public class AddNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl>
//{
//    private final String lxcHostname;
//    private final NodeType nodeType;
//
//
//    public AddNodeOperationHandler( AccumuloImpl manager, String clusterName, String lxcHostname, NodeType nodeType )
//    {
//        super( manager, clusterName );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
//        Preconditions.checkNotNull( nodeType, "Node type is null" );
//        this.lxcHostname = lxcHostname;
//        this.nodeType = nodeType;
//        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
//                String.format( "Adding node %s of type %s to %s", lxcHostname, nodeType, clusterName ) );
//    }
//
//
//    @Override
//    public UUID getTrackerId()
//    {
//        return trackerOperation.getId();
//    }
//
//
//    @Override
//    public void run()
//    {
//        //check of node type is allowed for addition
//        if ( !( nodeType == NodeType.Tracer || nodeType.isSlave() ) )
//        {
//            trackerOperation.addLogFailed( "Only tracer or slave node can be added" );
//            return;
//        }
//        //check if cluster exists
//        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
//        if ( accumuloClusterConfig == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
//            return;
//        }
//
//        //check if node's agent is connected
//        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
//        if ( lxcAgent == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Agent %s is not connected", lxcHostname ) );
//            return;
//        }
//
//        //check if node already belongs to specified role
//        if ( nodeType == NodeType.Tracer && accumuloClusterConfig.getTracers().contains( lxcAgent ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Agent %s already belongs to tracers", lxcHostname ) );
//            return;
//        }
//        else if ( nodeType.isSlave() && accumuloClusterConfig.getSlaves().contains( lxcAgent ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Agent %s already belongs to slaves", lxcHostname ) );
//            return;
//        }
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
//        //check if node has Hadoop installed
//        if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) )
//        {
//            trackerOperation
//                    .addLogFailed( String.format( "Node %s has no Hadoop installation", lxcAgent.getHostname() ) );
//            return;
//        }
//
//        //determine is Accumulo installation is needed
//        boolean install = !result.getStdOut().contains( Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME );
//
//        //check if underlying Hadoop cluster still exists
//        HadoopClusterConfig hadoopConfig =
//                manager.getHadoopManager().getCluster( accumuloClusterConfig.getHadoopClusterName() );
//
//        if ( hadoopConfig == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Hadoop cluster with name '%s' not found",
//                    accumuloClusterConfig.getClusterName() ) );
//            return;
//        }
//
//        //check if node belongs to underlying Hadoop cluster
//        if ( !hadoopConfig.getAllNodes().contains( lxcAgent ) )
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Node '%s' does not belong to Hadoop cluster %s", lxcAgent.getHostname(),
//                            accumuloClusterConfig.getClusterName() ) );
//            return;
//        }
//
//        //check if associated ZK cluster still exists
//        ZookeeperClusterConfig zkConfig =
//                manager.getZkManager().getCluster( accumuloClusterConfig.getZookeeperClusterName() );
//
//        if ( zkConfig == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Zookeeper cluster with name '%s' not found",
//                    accumuloClusterConfig.getClusterName() ) );
//            return;
//        }
//
//        //add node
//        try
//        {
//            new ClusterConfiguration( trackerOperation, manager )
//                    .addNode( accumuloClusterConfig, zkConfig, lxcAgent, nodeType, install );
//            trackerOperation.addLogDone( "Node added successfully" );
//        }
//        catch ( ClusterConfigurationException e )
//        {
//            trackerOperation.addLogFailed( String.format( "Node addition failed, %s", e.getMessage() ) );
//        }
//    }
//}
