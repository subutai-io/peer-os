package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StatusDataNodeOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private String lxcHostName;


    public StatusDataNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostName = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking DataNode in %s", clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( hadoopClusterConfig.getNameNode() == null ) {
            productOperation.addLogFailed( String.format( "DataNode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
        if ( node == null ) {
            productOperation.addLogFailed( "DataNode is not connected" );
            return;
        }
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

        if ( NodeState.UNKNOWN.equals( nodeState ) ) {
            productOperation.addLogFailed( String.format( "Failed to check status of %s", node.getHostname() ) );
        }
        else {
            productOperation.addLogDone( String.format( "DataNode of %s is %s", node.getHostname(), nodeState ) );
        }
    }
}
