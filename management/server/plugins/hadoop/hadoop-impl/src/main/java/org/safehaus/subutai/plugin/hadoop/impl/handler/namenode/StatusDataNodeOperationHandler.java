//package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;
//
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
//public class StatusDataNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
//{
//
//    private String lxcHostName;
//
//
//    public StatusDataNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostname )
//    {
//        super( manager, clusterName );
//        this.lxcHostName = lxcHostname;
//        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
//                String.format( "Checking DataNode in %s", clusterName ) );
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
//            trackerOperation.addLogFailed( String.format( "DataNode on %s does not exist", clusterName ) );
//            return;
//        }
//
//        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
//        if ( node == null )
//        {
//            trackerOperation.addLogFailed( "DataNode is not connected" );
//            return;
//        }
//        Command statusCommand = manager.getCommands().getNameNodeCommand( node, "status" );
//        manager.getCommandRunner().runCommand( statusCommand );
//
//        AgentResult result = statusCommand.getResults().get( node.getUuid() );
//
//        NodeState nodeState = NodeState.UNKNOWN;
//        if ( statusCommand.hasCompleted() )
//        {
//            if ( result.getStdOut() != null && result.getStdOut().contains( "DataNode" ) )
//            {
//                String[] array = result.getStdOut().split( "\n" );
//
//                for ( String status : array )
//                {
//                    if ( status.contains( "DataNode" ) )
//                    {
//                        String temp = status.replaceAll( "DataNode is ", "" );
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
//            trackerOperation.addLogFailed( String.format( "Failed to check status of %s", node.getHostname() ) );
//        }
//        else
//        {
//            trackerOperation.addLogDone( String.format( "DataNode of %s is %s", node.getHostname(), nodeState ) );
//        }
//    }
//}
