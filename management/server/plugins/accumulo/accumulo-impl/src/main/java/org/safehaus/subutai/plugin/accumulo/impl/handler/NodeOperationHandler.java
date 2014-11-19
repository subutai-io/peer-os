package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class NodeOperationHandler extends AbstractOperationHandler<AccumuloImpl, AccumuloClusterConfig>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;
    private NodeType nodeType;
    private Hadoop hadoop;
    private Zookeeper zookeeper;


    public NodeOperationHandler( final AccumuloImpl manager, final Hadoop hadoop, final Zookeeper zookeeper,
                                 final String clusterName, final String hostname, NodeOperationType operationType,
                                 NodeType nodeType )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.hadoop = hadoop;
        this.zookeeper = zookeeper;
        this.operationType = operationType;
        this.nodeType = nodeType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        AccumuloClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }

        try
        {
            CommandResult result = null;
            switch ( operationType )
            {
                case START:
                    result = host.execute( new RequestBuilder( Commands.startCommand ) );
                    break;
                case STOP:
                    result = host.execute( new RequestBuilder( Commands.stopCommand ) );
                    break;
                case STATUS:
                    result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                    break;
                case UNINSTALL:
                    result = uninstallProductOnNode( host, nodeType );
                    break;
                case INSTALL:
                    result = installProductOnNode( host, nodeType );
                    break;
            }
            assert result != null;
            trackerOperation.addLogDone( result.getStdOut() );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private CommandResult installProductOnNode( ContainerHost host, NodeType nodeType )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY.toLowerCase() )
                    .withTimeout( 3600 ) );
            if ( result.hasSucceeded() )
            {
                switch ( nodeType )
                {
                    case ACCUMULO_TRACER:
                        config.getTracers().add( host.getId() );
                        break;
                    case ACCUMULO_TABLET_SERVER:
                        config.getSlaves().add( host.getId() );
                        break;
                }

                // Configure all nodes again
                try
                {
                    Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID(
                            hadoop.getCluster( config.getHadoopClusterName() ).getEnvironmentId() );
                    new ClusterConfiguration( manager, trackerOperation ).configureCluster( environment, config,
                            zookeeper.getCluster( config.getZookeeperClusterName() ) );
                }
                catch ( ClusterConfigurationException e )
                {
                    e.printStackTrace();
                }

                manager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLog(
                        AccumuloClusterConfig.PRODUCT_KEY + " is installed on node " + host.getHostname()
                                + " successfully." );
            }
            else
            {
                trackerOperation.addLogFailed(
                        "Could not install " + AccumuloClusterConfig.PRODUCT_KEY + " to node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }


    private CommandResult uninstallProductOnNode( ContainerHost host, NodeType nodeType )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                            .toLowerCase() ) );
            if ( result.hasSucceeded() )
            {
                switch ( nodeType )
                {
                    case ACCUMULO_TRACER:
                        config.getTracers().add( host.getId() );
                        break;
                    case ACCUMULO_TABLET_SERVER:
                        config.getSlaves().add( host.getId() );
                        break;
                }

                // Configure all nodes again
                try
                {
                    Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID(
                            hadoop.getCluster( config.getHadoopClusterName() ).getEnvironmentId() );
                    new ClusterConfiguration( manager, trackerOperation ).configureCluster( environment, config,
                            zookeeper.getCluster( config.getZookeeperClusterName() ) );
                }
                catch ( ClusterConfigurationException e )
                {
                    e.printStackTrace();
                }


                manager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLog(
                        AccumuloClusterConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname()
                                + " successfully." );
            }
            else
            {
                trackerOperation.addLogFailed(
                        "Could not uninstall " + AccumuloClusterConfig.PRODUCT_KEY + " from node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }
}
