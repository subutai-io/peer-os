package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class IncludeNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String lxcHostName;


    public IncludeNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Including node %s again to cluster %s", lxcHostName, clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );

        if ( hadoopClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( !hadoopClusterConfig.getDataNodes().contains( node ) || !hadoopClusterConfig.getTaskTrackers()
                                                                                         .contains( node ) )
        {
            trackerOperation
                    .addLogFailed( String.format( "Node in %s cluster as a slave does not exist", clusterName ) );
            return;
        }

        if ( node == null )
        {
            trackerOperation.addLogFailed( "Node is not connected" );
            return;
        }

        Command addDataNodeCommand = manager.getCommands().getSetDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( addDataNodeCommand );
        logCommand( addDataNodeCommand, trackerOperation );

        Command includeDataNodeCommand = manager.getCommands().getExcludeDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeDataNodeCommand );
        logCommand( includeDataNodeCommand, trackerOperation );

        Command stopDataNodeCommand = manager.getCommands().getStopDatanodeCommand( node );
        manager.getCommandRunner().runCommand( stopDataNodeCommand );
        logCommand( stopDataNodeCommand, trackerOperation );

        Command startDataNodeCommand = manager.getCommands().getStartDataNodeCommand( node );
        manager.getCommandRunner().runCommand( startDataNodeCommand );
        logCommand( startDataNodeCommand, trackerOperation );

        Command refreshDataNodeCommand = manager.getCommands().getRefreshNameNodeCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshDataNodeCommand );
        logCommand( refreshDataNodeCommand, trackerOperation );


        Command addTaskTrackerCommand = manager.getCommands().getSetTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( addTaskTrackerCommand );
        logCommand( addTaskTrackerCommand, trackerOperation );

        Command includeTaskTrackerCommand =
                manager.getCommands().getExcludeTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeTaskTrackerCommand );
        logCommand( includeTaskTrackerCommand, trackerOperation );

        Command stopTaskTrackerCommand = manager.getCommands().getStopTaskTrackerCommand( node );
        manager.getCommandRunner().runCommand( stopTaskTrackerCommand );
        logCommand( stopTaskTrackerCommand, trackerOperation );

        Command startTaskTrackerCommand = manager.getCommands().getStartTaskTrackerCommand( node );
        manager.getCommandRunner().runCommand( startTaskTrackerCommand );
        logCommand( startTaskTrackerCommand, trackerOperation );

        Command refreshJobTrackerCommand = manager.getCommands().getRefreshJobTrackerCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        logCommand( refreshJobTrackerCommand, trackerOperation );


        hadoopClusterConfig.getBlockedAgents().remove( node );

        manager.getPluginDAO()
               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(), hadoopClusterConfig );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
        return;
    }


    private void logCommand( Command command, TrackerOperation po )
    {
        if ( command.hasSucceeded() )
        {
            po.addLog( String.format( "Task's operation %s finished", command.getDescription() ) );
        }
        else if ( command.hasCompleted() )
        {
            po.addLogFailed( String.format( "Task's operation %s failed", command.getDescription() ) );
        }
        else
        {
            po.addLogFailed( String.format( "Task's operation %s timeout", command.getDescription() ) );
        }
    }
}
