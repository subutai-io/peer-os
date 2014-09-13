package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StopDataNodeOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private String lxcHostName;

    public StopDataNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName ) {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Stopping Datanode in %s", clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( ! hadoopClusterConfig.getDataNodes().contains( lxcHostName ) ) {
            productOperation.addLogFailed( String.format( "Datanode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
        if ( node == null ) {
            productOperation.addLogFailed( "Datanode is not connected" );
            return;
        }

        Command startCommand = Commands.getStopDatanodeCommand( node );
        manager.getCommandRunner().runCommand( startCommand );
        Command statusCommand = Commands.getNameNodeCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );
        NodeState nodeState = NodeState.UNKNOWN;

        if ( statusCommand.hasCompleted() ) {
            if ( result.getStdOut() != null && result.getStdOut().contains( "DataNode" ) ) {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array ) {
                    if ( status.contains( "DataNode" ) ) {
                        String temp = status.replaceAll( "DataNode is ", "" );
                        if ( temp.toLowerCase().contains( "not" ) ) {
                            nodeState = NodeState.STOPPED;
                        }
                        else {
                            nodeState = NodeState.RUNNING;
                        }
                    }
                }
            }
        }

        if ( NodeState.STOPPED.equals( nodeState ) ) {
            productOperation.addLogDone( String.format( "Datanode on %s stopped", node.getHostname() ) );
        }
        else {
            productOperation.addLogFailed( String.format( "Failed to stop Datanode %s. %s", node.getHostname(),
                    startCommand.getAllErrors() ) );
        }
    }
}
