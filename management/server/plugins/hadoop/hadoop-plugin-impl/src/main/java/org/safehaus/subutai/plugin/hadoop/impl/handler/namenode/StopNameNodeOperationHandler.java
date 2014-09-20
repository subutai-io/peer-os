package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StopNameNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public StopNameNodeOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Stopping NameNode in %s", clusterName ) );
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
            productOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( hadoopClusterConfig.getNameNode().getHostname() );
        if ( node == null )
        {
            productOperation.addLogFailed( "NameNode is not connected" );
            return;
        }

        Command startCommand = Commands.getNameNodeCommand( node, "stop" );
        manager.getCommandRunner().runCommand( startCommand );
        Command statusCommand = Commands.getNameNodeCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );
        NodeState nodeState = NodeState.UNKNOWN;

        if ( result != null )
        {
            if ( result.getStdOut().contains( "NameNode" ) )
            {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array )
                {
                    if ( status.contains( "NameNode" ) )
                    {
                        String temp = status.replaceAll(
                                Pattern.quote( "!(SecondaryNameNode is not running on this " + "machine)" ), "" ).
                                                    replaceAll( "NameNode is ", "" );
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

        if ( NodeState.STOPPED.equals( nodeState ) )
        {
            productOperation.addLogDone( String.format( "NameNode on %s stopped", hadoopClusterConfig.getNameNode() ) );
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Failed to start NameNode %s. %s", hadoopClusterConfig.getNameNode(),
                            startCommand.getAllErrors() ) );
        }
    }
}
