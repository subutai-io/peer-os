package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;


/**
 * Configures Accumulo Cluster
 */
public class ClusterConfiguration
{

    private static final int TIMEOUT = 30;
    private TrackerOperation po;
    private AccumuloImpl accumuloManager;


    public ClusterConfiguration( final AccumuloImpl accumuloManager, final TrackerOperation po )
    {
        Preconditions.checkNotNull( accumuloManager, "Accumulo Manager is null" );
        Preconditions.checkNotNull( po, "Product Operation is null" );
        this.po = po;
        this.accumuloManager = accumuloManager;
    }


    public void configureCluster( Environment environment, AccumuloClusterConfig accumuloClusterConfig,
                                  ZookeeperClusterConfig zookeeperClusterConfig ) throws ClusterConfigurationException
    {

        po.addLog( "Configuring cluster..." );
        ContainerHost master = getHost( environment, accumuloClusterConfig.getMasterNode() );
        ContainerHost gc = getHost( environment, accumuloClusterConfig.getGcNode() );
        ContainerHost monitor = getHost( environment, accumuloClusterConfig.getMonitor() );


        /** configure cluster */
        for ( UUID uuid : accumuloClusterConfig.getAllNodes() )
        {
            ContainerHost host = getHost( environment, uuid );

            // configure master node
            executeCommand( host, Commands.getAddMasterCommand( master.getHostname() ) );

            // configure GC node
            executeCommand( host, Commands.getAddGCCommand( gc.getHostname() ) );

            // configure monitor node
            executeCommand( host, Commands.getAddMonitorCommand( monitor.getHostname() ) );

            // configure tracers
            executeCommand( host, Commands.getAddTracersCommand(
                    serializeSlaveNodeNames( environment, accumuloClusterConfig.getTracers() ) ) );

            // configure slaves
            executeCommand( host, Commands.getAddSlavesCommand(
                    serializeSlaveNodeNames( environment, accumuloClusterConfig.getSlaves() ) ) );

            // configure zookeeper
            executeCommand( host, Commands.getBindZKClusterCommand( serializeZKNodeNames( zookeeperClusterConfig ) ) );
        }

        // init accumulo instance
        executeCommand( master, Commands.getInitCommand( accumuloClusterConfig.getInstanceName(),
                accumuloClusterConfig.getPassword() ) );


        accumuloClusterConfig.setEnvironmentId( environment.getId() );
        accumuloManager.getPluginDAO()
                       .saveInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
                               accumuloClusterConfig );

        // start cluster
        po.addLog( "Starting cluster ..." );
        executeCommand( master, Commands.startCommand );

        po.addLogDone( AccumuloClusterConfig.PRODUCT_KEY + " cluster data saved into database" );
    }


    private String serializeSlaveNodeNames( Environment environment, Set<UUID> slaveNodes )
    {
        StringBuilder slavesSpaceSeparated = new StringBuilder();
        for ( UUID tracer : slaveNodes )
        {
            slavesSpaceSeparated.append( getHost( environment, tracer ).getHostname() ).append( " " );
        }
        return slavesSpaceSeparated.toString();
    }


    private String serializeZKNodeNames( ZookeeperClusterConfig zookeeperClusterConfig )
    {
        Environment environment = accumuloManager.getEnvironmentManager()
                                                 .getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
        Set<UUID> zkNodes = zookeeperClusterConfig.getNodes();
        StringBuilder zkNodesCommaSeparated = new StringBuilder();
        for ( UUID zkNode : zkNodes )
        {
            zkNodesCommaSeparated.append( getHost( environment, zkNode ).getHostname() ).append( ":2181," );
        }
        zkNodesCommaSeparated.delete( zkNodesCommaSeparated.length() - 1, zkNodesCommaSeparated.length() );
        return zkNodesCommaSeparated.toString();
    }


    private void executeCommand( ContainerHost host, String commnad )
    {
        try
        {
            host.execute( new RequestBuilder( commnad ).withTimeout( TIMEOUT ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }


    private ContainerHost getHost( Environment environment, UUID uuid )
    {
        return environment.getContainerHostByUUID( uuid );
    }
}
