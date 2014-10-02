package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String lxcHostName;


    public DestroyNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s node and updating cluster information of %s", lxcHostName,
                        clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );

        if ( hadoopClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( !hadoopClusterConfig.getDataNodes().contains( node ) || !hadoopClusterConfig.getTaskTrackers()
                                                                                         .contains( node ) )
        {
            productOperation
                    .addLogFailed( String.format( "Node in %s cluster as a slave does not exist", clusterName ) );
            return;
        }

        if ( node == null )
        {
            productOperation.addLogFailed( "Node is not connected" );
            return;
        }

        Command removeTaskTrackerCommand =
                manager.getCommands().getRemoveTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeTaskTrackerCommand );
        logCommand( removeTaskTrackerCommand, productOperation );

        Command excludeTaskTrackerCommand =
                manager.getCommands().getExcludeTaskTrackerCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( excludeTaskTrackerCommand );
        logCommand( excludeTaskTrackerCommand, productOperation );

        Command removeDataNodeCommand = manager.getCommands().getRemoveDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( removeDataNodeCommand );
        logCommand( removeDataNodeCommand, productOperation );

        Command excludeDataNodeCommand = manager.getCommands().getExcludeDataNodeCommand( hadoopClusterConfig, node );
        manager.getCommandRunner().runCommand( excludeDataNodeCommand );
        logCommand( excludeDataNodeCommand, productOperation );


        Command refreshJobTrackerCommand = manager.getCommands().getRefreshJobTrackerCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshJobTrackerCommand );
        productOperation.addLog( refreshJobTrackerCommand.getDescription() );


        Command refreshNameNodeCommand = manager.getCommands().getRefreshNameNodeCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( refreshNameNodeCommand );
        productOperation.addLog( refreshNameNodeCommand.getDescription() );

        productOperation.addLog( "Destroying lxc container " + lxcHostName + "..." );

        try
        {
            manager.getContainerManager().cloneDestroy( node.getParentHostName(), lxcHostName );
            productOperation.addLog( "Lxc container successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            productOperation
                    .addLogFailed( String.format( "Lxc container could not destroyed: %s, ", ex.getMessage() ) );
        }

        hadoopClusterConfig.removeNode( node );

        manager.getPluginDAO()
               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(), hadoopClusterConfig );
        productOperation.addLogDone( "Cluster info saved to DB" );
    }


    private void logCommand( Command command, ProductOperation po )
    {
        if ( command.hasSucceeded() )
        {
            po.addLog( String.format( "Task's operation %s succeeded", command.getDescription() ) );
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
