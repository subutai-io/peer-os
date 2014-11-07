package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import com.google.common.collect.Sets;


public class ChangeMasterNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String newMasterHostname;
    private final boolean keepSlave;


    public ChangeMasterNodeOperationHandler( SparkImpl manager, String clusterName, String newMasterHostname,
                                             boolean keepSlave )
    {
        super( manager, clusterName );
        this.newMasterHostname = newMasterHostname;
        this.keepSlave = keepSlave;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Changing master to %s in %s", newMasterHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        try
        {
            final SparkClusterConfig config = manager.getCluster( clusterName );
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

            ContainerHost master = environment.getContainerHostByUUID( config.getMasterNodeId() );

            if ( master == null )
            {
                throw new ClusterException(
                        String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
            }

            ContainerHost newMaster = environment.getContainerHostByHostname( newMasterHostname );

            if ( newMaster == null )
            {
                throw new ClusterException(
                        String.format( "Node not found in environment by name %s", newMasterHostname ) );
            }

            if ( newMaster.getId().equals( config.getMasterNodeId() ) )
            {
                throw new ClusterException( String.format( "Node %s is already a master node", newMasterHostname ) );
            }


            if ( !config.getAllNodesIds().contains( newMaster.getId() ) )
            {
                throw new ClusterException(
                        String.format( "Node %s does not belong to this cluster", newMasterHostname ) );
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


            trackerOperation.addLog( "Stopping all nodes..." );
            //stop all nodes
            RequestBuilder stopNodesCommand = manager.getCommands().getStopAllCommand();

            executeCommand( master, stopNodesCommand, false );

            trackerOperation.addLog( "Clearing slaves on old master..." );

            RequestBuilder clearSlavesCommand = manager.getCommands().getClearSlavesCommand();

            executeCommand( master, clearSlavesCommand, true );


            //add slaves to new master, if keepSlave=true then master node is also added as slave
            config.getSlaveIds().add( master.getId() );
            config.setMasterNodeId( newMaster.getId() );

            if ( keepSlave )
            {
                config.getSlaveIds().add( newMaster.getId() );
            }
            else
            {
                config.getSlaveIds().remove( newMaster.getId() );
            }

            trackerOperation.addLog( "Adding nodes to new master..." );

            Set<ContainerHost> slaves = environment.getHostsByIds( config.getSlaveIds() );
            Set<String> slaveHostnames = Sets.newHashSet();
            for ( ContainerHost slave : slaves )
            {
                slaveHostnames.add( slave.getHostname() );
            }

            RequestBuilder addSlavesCommand = manager.getCommands().getAddSlavesCommand( slaveHostnames );

            executeCommand( newMaster, addSlavesCommand, false );


            trackerOperation.addLog( "Setting new master IP..." );

            //modify master ip on all nodes
            RequestBuilder setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( newMasterHostname );

            for ( ContainerHost node : allNodes )
            {
                executeCommand( node, setMasterIPCommand, false );

                trackerOperation.addLog( String.format( "IP is set on node: %s", node.getHostname() ) );
            }


            trackerOperation.addLog( "Starting cluster..." );
            //start master & slaves

            RequestBuilder startNodesCommand = manager.getCommands().getStartAllCommand();

            executeCommand( newMaster, startNodesCommand, true );


            trackerOperation.addLog( "Updating db..." );
            //update db
            manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, config );
            trackerOperation.addLogDone( "Cluster info updated in DB\nDone" );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Failed to change master: %s", e.getMessage() ) );
        }
    }


    public void executeCommand( ContainerHost host, RequestBuilder command, boolean skipError ) throws ClusterException
    {

        CommandResult result = null;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            if ( skipError )
            {
                trackerOperation
                        .addLog( String.format( "Error on container %s: %s", host.getHostname(), e.getMessage() ) );
            }
            else
            {
                throw new ClusterException( e );
            }
        }
        if ( skipError )
        {
            if ( result != null && !result.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        else
        {
            if ( !result.hasSucceeded() )
            {
                throw new ClusterException( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
    }
}
