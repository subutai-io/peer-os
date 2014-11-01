package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class StartDataNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String lxcHostName;


    public StartDataNodeOperationHandler( HadoopImpl manager, String clusterName, String lxcHostName )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostName;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Starting Datanode in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        /*HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostName );
        if ( !hadoopClusterConfig.getDataNodes().contains( agent ) )
        {
            trackerOperation.addLogFailed( String.format( "Datanode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
        if ( node == null )
        {
            trackerOperation.addLogFailed( "Datanode is not connected" );
            return;
        }

        Command startCommand = manager.getCommands().getStartDataNodeCommand( node );
        manager.getCommandRunner().runCommand( startCommand );
        Command statusCommand = manager.getCommands().getNameNodeCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );
        NodeState nodeState = NodeState.UNKNOWN;

        if ( statusCommand.hasCompleted() )
        {
            if ( result.getStdOut() != null && result.getStdOut().contains( "DataNode" ) )
            {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array )
                {
                    if ( status.contains( "DataNode" ) )
                    {
                        String temp = status.replaceAll( "DataNode is ", "" );
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

        if ( NodeState.RUNNING.equals( nodeState ) )
        {
            trackerOperation.addLogDone( String.format( "Datanode on %s started", node.getHostname() ) );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Failed to start Datanode %s. %s", node.getHostname(),
                    startCommand.getAllErrors() ) );
        }*/
    }
}
