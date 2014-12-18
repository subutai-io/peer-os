package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class RemoveNodeOperationHandler extends AbstractOperationHandler<HadoopImpl, HadoopClusterConfig>
{

    private String lxcHostName;


    public RemoveNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.lxcHostName = lxcHostName;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s node and updating cluster information of %s", lxcHostName,
                        clusterName ) );
    }


    @Override
    public void run()
    {
        trackerOperation.addLogFailed( "Remove node functionality is not supported by Environment manager. Aborting!" );
        return;
        //        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        //        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
        //
        //        if ( hadoopClusterConfig == null )
        //        {
        //            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist",
        // clusterName ) );
        //            return;
        //        }
        //
        //        if ( !hadoopClusterConfig.getDataNodes().contains( node ) || !hadoopClusterConfig.getTaskTrackers()
        //                                                                                         .contains( node ) )
        //        {
        //            trackerOperation
        //                    .addLogFailed( String.format( "Node in %s cluster as a slave does not exist",
        // clusterName ) );
        //            return;
        //        }
        //
        //        if ( node == null )
        //        {
        //            trackerOperation.addLogFailed( "Node is not connected" );
        //            return;
        //        }
        //
        //        Command removeTaskTrackerCommand =
        //                manager.getCommands().getRemoveTaskTrackerCommand( hadoopClusterConfig, node );
        //        manager.getCommandRunner().runCommand( removeTaskTrackerCommand );
        //        logCommand( removeTaskTrackerCommand, trackerOperation );
        //
        //        Command excludeTaskTrackerCommand =
        //                manager.getCommands().getExcludeTaskTrackerCommand( hadoopClusterConfig, node );
        //        manager.getCommandRunner().runCommand( excludeTaskTrackerCommand );
        //        logCommand( excludeTaskTrackerCommand, trackerOperation );
        //
        //        Command removeDataNodeCommand = manager.getCommands().getRemoveDataNodeCommand(
        // hadoopClusterConfig, node );
        //        manager.getCommandRunner().runCommand( removeDataNodeCommand );
        //        logCommand( removeDataNodeCommand, trackerOperation );
        //
        //        Command excludeDataNodeCommand = manager.getCommands().getExcludeDataNodeCommand(
        // hadoopClusterConfig, node );
        //        manager.getCommandRunner().runCommand( excludeDataNodeCommand );
        //        logCommand( excludeDataNodeCommand, trackerOperation );
        //
        //
        //        Command refreshJobTrackerCommand = manager.getCommands().getRefreshJobTrackerCommand(
        // hadoopClusterConfig );
        //        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        //        trackerOperation.addLog( refreshJobTrackerCommand.getDescription() );
        //
        //
        //        Command refreshNameNodeCommand = manager.getCommands().getRefreshNameNodeCommand(
        // hadoopClusterConfig );
        //        manager.getCommandRunner().runCommand( refreshNameNodeCommand );
        //        trackerOperation.addLog( refreshNameNodeCommand.getDescription() );
        //
        //        trackerOperation.addLog( "Destroying lxc container " + lxcHostName + "..." );
        //
        //        try
        //        {
        //            manager.getContainerManager().cloneDestroy( node.getParentHostName(), lxcHostName );
        //            trackerOperation.addLog( "Lxc container successfully destroyed" );
        //        }
        //        catch ( LxcDestroyException ex )
        //        {
        //            trackerOperation
        //                    .addLogFailed( String.format( "Lxc container could not destroyed: %s, ",
        // ex.getMessage() ) );
        //        }
        //
        //        hadoopClusterConfig.removeNode( node );
        //
        //        manager.getPluginDAO()
        //               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
        // hadoopClusterConfig );
        //        trackerOperation.addLogDone( "Cluster info saved to DB" );
    }


    //    private void logCommand( Command command, TrackerOperation po )
    //    {
    //        if ( command.hasSucceeded() )
    //        {
    //            po.addLog( String.format( "Task's operation %s succeeded", command.getDescription() ) );
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
}
