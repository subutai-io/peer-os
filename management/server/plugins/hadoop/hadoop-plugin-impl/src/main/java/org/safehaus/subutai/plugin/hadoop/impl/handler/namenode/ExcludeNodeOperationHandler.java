package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class ExcludeNodeOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private String lxcHostName;


    public ExcludeNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName ) {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Excluding node from %s", clusterName ) );
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

        Command removeTaskTrackerCommand = Commands.getRemoveTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeTaskTrackerCommand );
        logCommand( removeTaskTrackerCommand, productOperation );

        Command includeTaskTrackerCommand = Commands.getIncludeTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeTaskTrackerCommand );
        logCommand( includeTaskTrackerCommand, productOperation );

        Command removeDataNodeCommand = Commands.getRemoveDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeDataNodeCommand );
        logCommand( removeDataNodeCommand, productOperation );

        Command includeDataNodeCommand = Commands.getIncludeDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( includeDataNodeCommand );
        logCommand( includeDataNodeCommand, productOperation );


        Command refreshJobTrackerCommand = Commands.getRefreshJobTrackerCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        logCommand( refreshJobTrackerCommand, productOperation );


        Command refreshNameNodeCommand = Commands.getRefreshNameNodeCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshNameNodeCommand );
        logCommand( refreshNameNodeCommand, productOperation );


        hadoopClusterConfig.getBlockedAgents().add( node );

        try {
            manager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                    hadoopClusterConfig );
            productOperation.addLog( "Cluster info saved to DB" );
            return;
        }
        catch ( DBException e ) {
            productOperation
                    .addLogFailed( "Could not save cluster info to DB! Please see logs\n" + "Blocking node aborted" );
            return;
        }
    }


    private void logCommand( Command command, ProductOperation po ) {
        if ( command.hasSucceeded() ) {
            po.addLogDone( String.format( "Task's operation %s finished", command.getDescription() ) );
        }
        else if ( command.hasCompleted() ) {
            po.addLogFailed( String.format( "Task's operation %s failed", command.getDescription() ) );
        }
        else {
            po.addLogFailed( String.format( "Task's operation %s timeout", command.getDescription() ) );
        }
    }
}
