package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class IncludeNodeOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private String lxcHostName;


    public IncludeNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName ) {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Including node %s again to cluster %s", lxcHostName, clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( ! hadoopClusterConfig.getDataNodes().contains( node ) ||
                ! hadoopClusterConfig.getTaskTrackers().contains( node ) ) {
            productOperation.addLogFailed( String.format( "Node in %s cluster as a slave does not exist", clusterName ) );
            return;
        }

        if ( node == null ) {
            productOperation.addLogFailed( "Node is not connected" );
            return;
        }

        Command addDataNodeCommand = Commands.getSetDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( addDataNodeCommand );
        logCommand( addDataNodeCommand, productOperation );

        Command includeDataNodeCommand = Commands.getExcludeDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeDataNodeCommand );
        logCommand( includeDataNodeCommand, productOperation );

        Command stopDataNodeCommand = Commands.getStopDatanodeCommand( node );
        manager.getCommandRunner().runCommand( stopDataNodeCommand );
        logCommand( stopDataNodeCommand, productOperation );

        Command startDataNodeCommand = Commands.getStartDataNodeCommand( node );
        manager.getCommandRunner().runCommand( startDataNodeCommand );
        logCommand( startDataNodeCommand, productOperation );

        Command refreshDataNodeCommand = Commands.getRefreshNameNodeCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshDataNodeCommand );
        logCommand( refreshDataNodeCommand, productOperation );


        Command addTaskTrackerCommand = Commands.getSetTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( addTaskTrackerCommand );
        logCommand( addTaskTrackerCommand, productOperation );

        Command includeTaskTrackerCommand = Commands.getExcludeTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeTaskTrackerCommand );
        logCommand( includeTaskTrackerCommand, productOperation );

        Command stopTaskTrackerCommand = Commands.getStopTaskTrackerCommand( node );
        manager.getCommandRunner().runCommand( stopTaskTrackerCommand );
        logCommand( stopTaskTrackerCommand, productOperation );

        Command startTaskTrackerCommand = Commands.getStartTaskTrackerCommand( node );
        manager.getCommandRunner().runCommand( startTaskTrackerCommand );
        logCommand( startTaskTrackerCommand, productOperation );

        Command refreshJobTrackerCommand = Commands.getRefreshJobTrackerCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        logCommand( refreshJobTrackerCommand, productOperation );


        hadoopClusterConfig.getBlockedAgents().remove( node );

        manager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                hadoopClusterConfig );
        productOperation.addLogDone( "Cluster info saved to DB" );
        return;
    }


    private void logCommand( Command command, ProductOperation po ) {
        if ( command.hasSucceeded() ) {
            po.addLog( String.format( "Task's operation %s finished", command.getDescription() ) );
        }
        else if ( command.hasCompleted() ) {
            po.addLogFailed( String.format( "Task's operation %s failed", command.getDescription() ) );
        }
        else {
            po.addLogFailed( String.format( "Task's operation %s timeout", command.getDescription() ) );
        }
    }
}
