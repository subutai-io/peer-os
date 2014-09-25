package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class CheckMasterNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{
    private final String lxcHostname;


    public CheckMasterNodeOperationHandler( SparkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !config.getAllNodes().contains( node ) )
        {
            productOperation.addLogFailed( String.format( "Node %s does not belong to this cluster", lxcHostname ) );
            return;
        }

        Command checkNodeCommand = Commands.getStatusMasterCommand( node );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        AgentResult res = checkNodeCommand.getResults().get( node.getUuid() );
        if ( checkNodeCommand.hasSucceeded() )
        {
            productOperation.addLogDone( String.format( "%s", res.getStdOut() ) );
        }
        else
        {
            productOperation
                    .addLogFailed( String.format( "Failed to check status, %s", checkNodeCommand.getAllErrors() ) );
        }
    }
}
