//package org.safehaus.subutai.plugin.zookeeper.impl.handler;
//
//
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.protocol.Response;
//import org.safehaus.subutai.core.command.api.command.AgentResult;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.core.command.api.command.CommandCallback;
//import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
//import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
//
//import com.google.common.collect.Sets;
//
//
///**
// * Handles start node operation
// */
//public class StartNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
//{
//    private final String lxcHostname;
//
//
//    public StartNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
//    {
//        super( manager, clusterName );
//        this.lxcHostname = lxcHostname;
//        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
//                String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
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
//        ZookeeperClusterConfig config = manager.getCluster( clusterName );
//        if ( config == null )
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
//            return;
//        }
//
//        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
//        if ( node == null )
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
//            return;
//        }
//
//        if ( !config.getNodes().contains( node ) )
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
//            return;
//        }
//
//        trackerOperation.addLog( "Starting node..." );
//
//        Command startCommand = manager.getCommands().getStartCommand( Sets.newHashSet( node ) );
//        final AtomicBoolean ok = new AtomicBoolean();
//        manager.getCommandRunner().runCommand( startCommand, new CommandCallback()
//        {
//            @Override
//            public void onResponse( Response response, AgentResult agentResult, Command command )
//            {
//                if ( agentResult.getStdOut().contains( "STARTED" ) )
//                {
//                    ok.set( true );
//                    stop();
//                }
//            }
//        } );
//
//        if ( ok.get() )
//        {
//            trackerOperation.addLogDone( String.format( "Node on %s started", lxcHostname ) );
//        }
//        else
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Failed to start node %s, %s", lxcHostname, startCommand.getAllErrors() ) );
//        }
//    }
//}
