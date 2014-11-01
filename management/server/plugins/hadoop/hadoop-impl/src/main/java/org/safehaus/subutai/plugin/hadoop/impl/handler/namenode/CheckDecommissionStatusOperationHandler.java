package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class CheckDecommissionStatusOperationHandler extends AbstractOperationHandler<HadoopImpl>
{


    public CheckDecommissionStatusOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking decommissioning status of %s", clusterName ) );
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

        if ( hadoopClusterConfig.getNameNode() == null )
        {
            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( hadoopClusterConfig.getNameNode().getHostname() );
        if ( node == null )
        {
            trackerOperation.addLogFailed( "NameNode is not connected" );
            return;
        }

        Command statusCommand = manager.getCommands().getReportHadoopCommand( hadoopClusterConfig );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( hadoopClusterConfig.getNameNode().getUuid() );

        if ( statusCommand.hasCompleted() )
        {
            trackerOperation.addLogDone( result.getStdOut() );
        }*/
    }
}
