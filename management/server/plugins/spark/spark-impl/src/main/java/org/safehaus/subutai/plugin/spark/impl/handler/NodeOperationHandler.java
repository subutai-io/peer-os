package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


public class NodeOperationHandler extends AbstractOperationHandler<SparkImpl, SparkClusterConfig>
{
    private static final Logger LOG = LoggerFactory.getLogger( NodeOperationHandler.class.getName() );

    private String hostname;
    private OperationType operationType;
    private NodeType nodeType;
    private Environment environment;
    private ContainerHost node;


    public NodeOperationHandler( final SparkImpl manager, final SparkClusterConfig config, final String hostname,
                                 OperationType operationType, NodeType nodeType )
    {
        super( manager, config );
        this.hostname = hostname;
        this.operationType = operationType;
        this.nodeType = nodeType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Executing %s operation on node %s", operationType.name(), hostname ) );
    }


    @Override
    public void run()
    {
        try
        {
            if ( manager.getCluster( clusterName ) == null )
            {
                throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
            }

            environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            node = environment.getContainerHostByHostname( hostname );

            if ( node == null )
            {
                throw new ClusterException( String.format( "Node not found in environment by name %s", hostname ) );
            }


            if ( !node.isConnected() )
            {
                throw new ClusterException( String.format( "Node %s is not connected", hostname ) );
            }


            switch ( operationType )
            {
                case START:
                    startNode();
                    break;
                case STOP:
                    stopNode();
                    break;
                case STATUS:
                    checkNode();
                    break;
                case INCLUDE:
                    addSlaveNode();
                    break;
                case EXCLUDE:
                    removeSlaveNode();
                    break;
                case CHANGE_MASTER:
                    changeMaster();
                    break;
            }


            trackerOperation.addLogDone( String.format( "Operation %s succeeded", operationType.name() ) );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in NodeOperationHandler", e );
            trackerOperation
                    .addLogFailed( String.format( "Operation %s failed: %s", operationType.name(), e.getMessage() ) );
        }
    }


    public void startNode() throws ClusterException
    {
        CommandResult result = executeCommand( node,
                nodeType == NodeType.MASTER_NODE ? manager.getCommands().getStartMasterCommand() :
                manager.getCommands().getStartSlaveCommand() );
        if ( !result.getStdOut().contains( "starting" ) )
        {
            throw new ClusterException( String.format( "Failed to start node %s", node.getHostname() ) );
        }
    }


    public void stopNode() throws ClusterException
    {
        executeCommand( node, nodeType == NodeType.MASTER_NODE ? manager.getCommands().getStopMasterCommand() :
                              manager.getCommands().getStopSlaveCommand() );
    }


    public void checkNode() throws ClusterException
    {
        executeCommand( node, nodeType == NodeType.MASTER_NODE ? manager.getCommands().getStatusMasterCommand() :
                              manager.getCommands().getStatusSlaveCommand() );
    }


    public void addSlaveNode() throws ClusterException
    {
        ContainerHost master = environment.getContainerHostById( config.getMasterNodeId() );

        if ( master == null )
        {
            throw new ClusterException(
                    String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
        }


        if ( !master.isConnected() )
        {
            throw new ClusterException( "Master node is not connected" );
        }


        //check if node is in the cluster
        if ( config.getAllNodesIds().contains( node.getId() ) )
        {
            throw new ClusterException( String.format( "Node %s already belongs to this cluster", hostname ) );
        }

        // check if node is one of Hadoop cluster nodes
        if ( !config.getHadoopNodeIds().contains( node.getId() ) )
        {
            throw new ClusterException( "Node does not belong to Hadoop cluster" );
        }

        //check if node already belongs to some existing Spark cluster
        List<SparkClusterConfig> clusters = manager.getClusters();

        for ( SparkClusterConfig cluster : clusters )
        {
            if ( cluster.getAllNodesIds().contains( node.getId() ) )
            {
                throw new ClusterException(
                        String.format( "Node %s already belongs to Spark cluster %s", node.getHostname(),
                                cluster.getClusterName() ) );
            }
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        //if the slave already contains master then we don't need to install Spark since it is already installed
        boolean install = !node.getId().equals( config.getMasterNodeId() );

        //check installed subutai packages
        RequestBuilder checkInstalledCommand = manager.getCommands().getCheckInstalledCommand();
        CommandResult result = executeCommand( node, checkInstalledCommand );


        if ( install )
        {
            //install only if container does not have Spark installed
            install = !result.getStdOut()
                             .contains( Common.PACKAGE_PREFIX + SparkClusterConfig.PRODUCT_KEY.toLowerCase() );
        }

        if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_KEY.toLowerCase() ) )
        {
            throw new ClusterException( String.format( "Node %s has no Hadoop installation", hostname ) );
        }

        config.getSlaveIds().add( node.getId() );

        //install spark
        if ( install )
        {
            trackerOperation.addLog( "Installing Spark..." );
            RequestBuilder installCommand = manager.getCommands().getInstallCommand();
            executeCommand( node, installCommand );
        }

        trackerOperation.addLog( "Setting master IP on slave..." );
        RequestBuilder setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( master.getHostname() );
        executeCommand( node, setMasterIPCommand );


        trackerOperation.addLog( "Registering slave with master..." );

        RequestBuilder addSlaveCommand = manager.getCommands().getAddSlaveCommand( node.getHostname() );
        executeCommand( master, addSlaveCommand );


        trackerOperation.addLog( "Restarting master..." );

        RequestBuilder restartMasterCommand = manager.getCommands().getRestartMasterCommand();
        result = executeCommand( master, restartMasterCommand );


        if ( !result.getStdOut().contains( "starting" ) )
        {
            trackerOperation.addLog( "Master restart failed, skipping..." );
        }


        trackerOperation.addLog( "Starting Spark on new node..." );

        RequestBuilder startSlaveCommand = manager.getCommands().getStartSlaveCommand();
        result = executeCommand( node, startSlaveCommand );

        if ( !result.getStdOut().contains( "starting" ) )
        {
            trackerOperation.addLog( "Slave start failed, skipping..." );
        }


        trackerOperation.addLog( "Updating db..." );
        if ( !manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, config ) )
        {
            throw new ClusterException( "Could not update cluster info" );
        }
    }


    public void removeSlaveNode() throws ClusterException
    {

        if ( config.getSlaveIds().size() == 1 )
        {
            throw new ClusterException( "This is the last slave node in the cluster. Please, destroy cluster instead" );
        }

        if ( node.getId().equals( config.getMasterNodeId() ) )
        {
            throw new ClusterException( "Can not destroy master node, change master first" );
        }

        if ( !config.getAllNodesIds().contains( node.getId() ) )
        {
            throw new ClusterException( String.format( "Node %s does not belong to this cluster", hostname ) );
        }


        ContainerHost master = environment.getContainerHostById( config.getMasterNodeId() );

        if ( master == null )
        {
            throw new ClusterException(
                    String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
        }


        if ( !master.isConnected() )
        {
            throw new ClusterException( "Master node is not connected" );
        }


        trackerOperation.addLog( "Unregistering slave from master..." );

        RequestBuilder clearSlavesCommand = manager.getCommands().getClearSlaveCommand( hostname );

        executeCommand( master, clearSlavesCommand );

        trackerOperation.addLog( "Successfully unregistered slave from master\nRestarting master..." );

        RequestBuilder restartMasterCommand = manager.getCommands().getRestartMasterCommand();

        CommandResult result = executeCommand( master, restartMasterCommand );

        if ( !result.getStdOut().contains( "starting" ) )
        {
            trackerOperation.addLog( "Master restart failed, skipping..." );
        }


        boolean uninstall = !node.getId().equals( config.getMasterNodeId() );

        if ( uninstall )
        {
            trackerOperation.addLog( "Uninstalling Spark..." );

            RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();

            executeCommand( node, uninstallCommand );
        }
        else
        {
            trackerOperation.addLog( "Stopping slave..." );

            RequestBuilder stopSlaveCommand = manager.getCommands().getStopSlaveCommand();

            executeCommand( node, stopSlaveCommand );
        }

        config.getSlaveIds().remove( node.getId() );

        trackerOperation.addLog( "Updating db..." );

        if ( !manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, config ) )
        {
            throw new ClusterException( "Could not update cluster info" );
        }
    }


    public void changeMaster() throws ClusterException
    {

        if ( !config.getAllNodesIds().contains( node.getId() ) )
        {
            throw new ClusterException( String.format( "Node %s does not belong to this cluster", hostname ) );
        }

        ContainerHost master = environment.getContainerHostById( config.getMasterNodeId() );

        if ( master == null )
        {
            throw new ClusterException(
                    String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
        }

        if ( !master.isConnected() )
        {
            throw new ClusterException( "Master node is not connected" );
        }


        if ( node.getId().equals( config.getMasterNodeId() ) )
        {
            throw new ClusterException( String.format( "Node %s is already a master node", hostname ) );
        }


        Set<ContainerHost> allNodes = environment.getContainerHostsByIds( config.getSlaveIds() );
        allNodes.add( environment.getContainerHostById( config.getMasterNodeId() ) );

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
        config.setMasterNodeId( node.getId() );

        boolean keepSlave = nodeType == NodeType.SLAVE_NODE;
        if ( keepSlave )
        {
            config.getSlaveIds().add( node.getId() );
        }
        else
        {
            config.getSlaveIds().remove( node.getId() );
        }

        trackerOperation.addLog( "Adding nodes to new master..." );

        Set<ContainerHost> slaves = environment.getContainerHostsByIds( config.getSlaveIds() );
        Set<String> slaveHostnames = Sets.newHashSet();
        for ( ContainerHost slave : slaves )
        {
            slaveHostnames.add( slave.getHostname() );
        }

        RequestBuilder addSlavesCommand = manager.getCommands().getAddSlavesCommand( slaveHostnames );

        executeCommand( node, addSlavesCommand, false );


        trackerOperation.addLog( "Setting new master IP..." );

        //modify master ip on all nodes
        RequestBuilder setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( hostname );

        for ( ContainerHost node : allNodes )
        {
            executeCommand( node, setMasterIPCommand, false );

            trackerOperation.addLog( String.format( "IP is set on node: %s", node.getHostname() ) );
        }


        trackerOperation.addLog( "Starting cluster..." );
        //start master & slaves

        RequestBuilder startNodesCommand = manager.getCommands().getStartAllCommand();

        CommandResult result = executeCommand( node, startNodesCommand, true );

        if ( !result.getStdOut().contains( "starting" ) )
        {
            trackerOperation.addLog( "Failed to start cluster, skipping..." );
        }


        trackerOperation.addLog( "Updating db..." );
        //update db
        if ( !manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, config ) )
        {
            throw new ClusterException( "Could not update cluster info" );
        }
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterException
    {

        return executeCommand( host, command, false );
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command, boolean skipError )
            throws ClusterException
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
        return result;
    }
}
