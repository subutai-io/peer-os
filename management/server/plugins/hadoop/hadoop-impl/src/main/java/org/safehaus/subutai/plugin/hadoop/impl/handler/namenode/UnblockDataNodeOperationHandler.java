//package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;
//
//
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
//import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
//
//
//public class UnblockDataNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
//{
//
//    private String lxcHostName;
//
//
//    public UnblockDataNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
//    {
//        super( manager, clusterName );
//        this.lxcHostName = lxcHostName;
//        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
//                String.format( "Unblocking DataNode in %s", clusterName ) );
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
//        if ( hadoopClusterConfig.getJobTracker() == null )
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
//
//        Command removeCommand = manager.getCommands().getSetDataNodeCommand( hadoopClusterConfig, node );
//        manager.getCommandRunner().runCommand( removeCommand );
//        logCommand( removeCommand, trackerOperation );
//
//        Command includeCommand = manager.getCommands().getExcludeDataNodeCommand( hadoopClusterConfig, node );
//        manager.getCommandRunner().runCommand( includeCommand );
//        logCommand( includeCommand, trackerOperation );
//
//        Command refreshCommand = manager.getCommands().getStartDataNodeCommand( node );
//        manager.getCommandRunner().runCommand( refreshCommand );
//        logCommand( refreshCommand, trackerOperation );
//
//        hadoopClusterConfig.getBlockedAgents().remove( node );
//        manager.getPluginDAO()
//               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
// hadoopClusterConfig );
//        trackerOperation.addLogDone( "Cluster info saved to DB" );
//    }
//
//
//    private void logCommand( Command command, TrackerOperation po )
//    {
//        if ( command.hasSucceeded() )
//        {
//            po.addLog( String.format( "Task's operation %s finished", command.getDescription() ) );
//        }
//        else if ( command.hasCompleted() )
//        {
//            po.addLogFailed( String.format( "Task's operation %s failed", command.getDescription() ) );
//        }
//        else
//        {
//            po.addLogFailed( String.format( "Task's operation %s timeout", command.getDescription() ) );
//        }
//    }
//}
