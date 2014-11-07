package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    public UninstallOperationHandler( SparkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        try
        {
            SparkClusterConfig config = manager.getCluster( clusterName );
            if ( config == null )
            {
                throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
            }


            Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            Set<ContainerHost> allNodes = environment.getHostsByIds( config.getSlaveIds() );
            allNodes.add( environment.getContainerHostByUUID( config.getMasterNodeId() ) );

            for ( ContainerHost node : allNodes )
            {
                if ( !node.isConnected() )
                {
                    throw new ClusterException( String.format( "Node %s is not connected", node.getHostname() ) );
                }
            }


            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                uninstall( allNodes );
            }
            else if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {
                destroyNodes( allNodes );
            }

            trackerOperation.addLogFailed( "Cluster uninstalled successfully" );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Failed to uninstall cluster: %s", e.getMessage() ) );
        }
    }


    private void uninstall( Set<ContainerHost> allNodes ) throws ClusterException
    {
        trackerOperation.addLog( "Uninstalling Spark..." );

        RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();
        for ( ContainerHost node : allNodes )
        {
            try
            {
                CommandResult result = node.execute( uninstallCommand );
                if ( !result.hasSucceeded() )
                {
                    throw new ClusterException(
                            String.format( "Could not uninstall Spark from node %s : %s", node.getHostname(),
                                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterException( String.format( "Error uninstalling Spark on node %s", node.getHostname() ),
                        e );
            }
        }
    }


    private void destroyNodes( Set<ContainerHost> allNodes ) throws ClusterException
    {

        //TODO add logic when Environment resize is implemented
    }
}
