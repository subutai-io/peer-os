package org.safehaus.subutai.plugin.shark.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class ActualizeMasterIpOperationHandler extends AbstractOperationHandler<SharkImpl>
{

    public ActualizeMasterIpOperationHandler( SharkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( SharkClusterConfig.PRODUCT_KEY,
                String.format( "Actualizing master IP of %s", clusterName ) );
    }


    @Override
    public void run()
    {
        SharkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( config.getSparkClusterName() == null )
        {
            trackerOperation.addLogFailed( "Spark cluster name not specified" );
            return;
        }
        SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster( config.getSparkClusterName() );
        if ( sparkConfig == null )
        {
            trackerOperation.addLogFailed( "Underlying Spark cluster not found: " + clusterName );
            return;
        }

        for ( Agent node : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                trackerOperation.addLogFailed(
                        String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        Command setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( config.getNodes(), sparkConfig.getMasterNode() );
        manager.getCommandRunner().runCommand( setMasterIPCommand );

        if ( setMasterIPCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Master IP actualized successfully\nDone" );
        }
        else
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to actualize Master IP, %s", setMasterIPCommand.getAllErrors() ) );
        }
    }
}

