package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class ExcludeNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String lxcHostName;


    public ExcludeNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Excluding node %s from %s", lxcHostName, clusterName ) );
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

        Command removeTaskTrackerCommand =
                manager.getCommands().getRemoveTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeTaskTrackerCommand );
        logCommand( removeTaskTrackerCommand, trackerOperation );

        Command includeTaskTrackerCommand =
                manager.getCommands().getIncludeTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeTaskTrackerCommand );
        logCommand( includeTaskTrackerCommand, trackerOperation );

        Command removeDataNodeCommand = manager.getCommands().getRemoveDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeDataNodeCommand );
        logCommand( removeDataNodeCommand, trackerOperation );

        Command includeDataNodeCommand = manager.getCommands().getIncludeDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeDataNodeCommand );
        logCommand( includeDataNodeCommand, trackerOperation );


        Command refreshJobTrackerCommand = manager.getCommands().getRefreshJobTrackerCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        logCommand( refreshJobTrackerCommand, trackerOperation );


        Command refreshNameNodeCommand = manager.getCommands().getRefreshNameNodeCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshNameNodeCommand );
        logCommand( refreshNameNodeCommand, trackerOperation );


        hadoopClusterConfig.getBlockedAgents().add( node );

        manager.getPluginDAO()
               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(), hadoopClusterConfig );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
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
