package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckNodeHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.InstallClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StartClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StartServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StopClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StopServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.UninstallClusterHandler;
import org.safehaus.subutai.plugin.common.PluginDAO;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class CassandraImpl implements Cassandra
{

    private Commands commands;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private PluginDAO pluginDAO;


    public CassandraImpl()
    {

    }


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public LxcManager getLxcManager()
    {
        return lxcManager;
    }


    public void setLxcManager( final LxcManager lxcManager )
    {
        this.lxcManager = lxcManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public void init()
    {
        this.pluginDAO = new PluginDAO( dbManager );
        this.commands = new Commands( commandRunner );

        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public UUID installCluster( final CassandraClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallClusterHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<CassandraClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( CassandraClusterConfig.PRODUCT_KEY, CassandraClusterConfig.class );
    }


    @Override
    public CassandraClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( CassandraClusterConfig.PRODUCT_KEY, clusterName, CassandraClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID startCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StartClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new CheckClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StopClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new StartServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new StopServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new CheckServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String lxchostname, final String nodetype )
    {
        // TODO
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxchostname, final String nodetype )
    {
        // TODO
        return null;
    }


    @Override
    public UUID checkNode( final String clustername, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new CheckNodeHandler( this, clustername, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final CassandraClusterConfig config,
                                                         final ProductOperation po )
    {
        return new CassandraSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( final CassandraClusterConfig config )
    {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil
                        .generateTimeBasedUUID() ) );

        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );

        return environmentBuildTask;
    }
}
