package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StatusSecondaryNameNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public StatusSecondaryNameNodeOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking Secondary NameNode in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( hadoopClusterConfig.getNameNode() == null )
        {
            productOperation.addLogFailed( String.format( "Secondary NameNode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager()
                            .getAgentByHostname( hadoopClusterConfig.getSecondaryNameNode().getHostname() );
        if ( node == null )
        {
            productOperation.addLogFailed( "Secondary NameNode is not connected" );
            return;
        }
        Command statusCommand = Commands.getNameNodeCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );

        NodeState nodeState = NodeState.UNKNOWN;
        if ( statusCommand.hasCompleted() )
        {
            if ( result.getStdOut() != null && result.getStdOut().contains( "SecondaryNameNode" ) )
            {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array )
                {
                    if ( status.contains( "SecondaryNameNode" ) )
                    {
                        String temp = status.
                                                    replaceAll( "SecondaryNameNode is ", "" );
                        if ( temp.toLowerCase().contains( "not" ) )
                        {
                            nodeState = NodeState.STOPPED;
                        }
                        else
                        {
                            nodeState = NodeState.RUNNING;
                        }
                    }
                }
            }
        }

        if ( NodeState.UNKNOWN.equals( nodeState ) )
        {
            productOperation.addLogFailed( String.format( "Failed to check status of %s, %s", node.getHostname(),
                    statusCommand.getAllErrors() ) );
        }
        else
        {
            productOperation
                    .addLogDone( String.format( "Secondary NameNode %s is %s", node.getHostname(), nodeState ) );
        }
    }
}
