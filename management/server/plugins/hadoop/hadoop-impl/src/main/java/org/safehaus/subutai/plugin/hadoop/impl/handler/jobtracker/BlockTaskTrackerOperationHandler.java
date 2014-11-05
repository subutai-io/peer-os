//package org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker;
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
//public class BlockTaskTrackerOperationHandler extends AbstractOperationHandler<HadoopImpl>
//{
//
//    private String lxcHostName;
//
//
//    public BlockTaskTrackerOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
//    {
//        super( manager, clusterName );
//        this.lxcHostName = lxcHostName;
//        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
//                String.format( "Blocking TaskTracker in %s", clusterName ) );
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
//            trackerOperation.addLogFailed( String.format( "TaskTracker on %s does not exist", clusterName ) );
//            return;
//        }
//
//        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
//        if ( node == null )
//        {
//            trackerOperation.addLogFailed( "TaskTracker is not connected" );
//            return;
//        }
//
//        Command removeCommand = manager.getCommands().getRemoveTaskTrackerCommand( hadoopClusterConfig, node );
//        manager.getCommandRunner().runCommand( removeCommand );
//        logCommand( removeCommand, trackerOperation );
//
//        Command includeCommand = manager.getCommands().getIncludeTaskTrackerCommand( hadoopClusterConfig, node );
//        manager.getCommandRunner().runCommand( includeCommand );
//        logCommand( includeCommand, trackerOperation );
//
//        Command refreshCommand = manager.getCommands().getRefreshJobTrackerCommand( hadoopClusterConfig );
//        manager.getCommandRunner().runCommand( refreshCommand );
//        logCommand( refreshCommand, trackerOperation );
//
//        hadoopClusterConfig.getBlockedAgents().add( node );
//        manager.getPluginDAO()
//               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(), hadoopClusterConfig );
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
