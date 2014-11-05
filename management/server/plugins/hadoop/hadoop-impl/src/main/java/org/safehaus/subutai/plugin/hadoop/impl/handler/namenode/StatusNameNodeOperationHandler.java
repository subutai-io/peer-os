//package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;
//
//
//import java.util.regex.Pattern;
//
//import org.safehaus.subutai.common.enums.NodeState;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.core.command.api.command.AgentResult;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
//import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
//
//
//public class StatusNameNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
//{
//
//    public StatusNameNodeOperationHandler( HadoopImpl manager, String clusterName )
//    {
//        super( manager, clusterName );
//        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
//                String.format( "Checking NameNode in %s", clusterName ) );
//    }
//
//
//    @Override
//    public void run()
//    {
//        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
//
//        if ( hadoopClusterConfig == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
//            return;
//        }
//
//        if ( hadoopClusterConfig.getNameNode() == null )
//        {
//            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
//            return;
//        }
//
//        Agent node = manager.getAgentManager().getAgentByHostname( hadoopClusterConfig.getNameNode().getHostname() );
//        if ( node == null )
//        {
//            trackerOperation.addLogFailed( "NameNode is not connected" );
//            return;
//        }
//        Command statusCommand = manager.getCommands().getNameNodeCommand( node, "status" );
//        manager.getCommandRunner().runCommand( statusCommand );
//
//        AgentResult result = statusCommand.getResults().get( node.getUuid() );
//        NodeState nodeState = NodeState.UNKNOWN;
//
//        if ( result != null )
//        {
//            if ( result.getStdOut().contains( "NameNode" ) )
//            {
//                String[] array = result.getStdOut().split( "\n" );
//
//                for ( String status : array )
//                {
//                    if ( status.contains( "NameNode" ) )
//                    {
//                        String temp = status.replaceAll(
//                                Pattern.quote( "!(SecondaryNameNode is not running on this " + "machine)" ), "" ).
//                                                    replaceAll( "NameNode is ", "" );
//                        if ( temp.toLowerCase().contains( "not" ) )
//                        {
//                            nodeState = NodeState.STOPPED;
//                        }
//                        else
//                        {
//                            nodeState = NodeState.RUNNING;
//                        }
//                    }
//                }
//            }
//        }
//
//        if ( NodeState.UNKNOWN.equals( nodeState ) )
//        {
//            trackerOperation.addLogFailed( String.format( "Failed to check status of %s, %s", node.getHostname(),
//                    statusCommand.getAllErrors() ) );
//        }
//        else
//        {
//            trackerOperation.addLogDone( String.format( "NameNode %s is %s", node.getHostname(), nodeState ) );
//        }
//    }
//}
