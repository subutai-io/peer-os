package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.CheckClusterOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.StartClusterOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.StopClusterOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.UninstallOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class ElasticsearchImpl implements Elasticsearch
{
    private static final Logger LOG = LoggerFactory.getLogger( ElasticsearchImpl.class.getName() );
    DataSource dataSource;
    private Tracker tracker;
    //    private LxcManager lxcManager;
    private ExecutorService executor;
    //    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private PluginDao pluginDAO;
    private ContainerManager containerManager;
    private EnvironmentManager environmentManager;
    private Commands commands;


    public ElasticsearchImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public PluginDao getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDao pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


   /* public void setLxcManager( final LxcManager lxcManager )
    {
        this.lxcManager = lxcManager;
    }*/


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    /*public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }*/


    public void setDataSource( final DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDao( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands( commandRunner );

        executor = Executors.newCachedThreadPool();
    }


    public Commands getCommands()
    {
        return commands;
    }


    public void setCommands( final Commands commands )
    {
        this.commands = commands;
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public UUID installCluster( final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration )
    {
        AbstractOperationHandler operationHandler =
                new InstallOperationHandler( this, elasticsearchClusterConfiguration );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<ElasticsearchClusterConfiguration> getClusters()
    {
        return pluginDAO
                .getInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, ElasticsearchClusterConfiguration.class );
    }


    @Override
    public ElasticsearchClusterConfiguration getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, clusterName,
                ElasticsearchClusterConfiguration.class );
    }


    @Override
    public UUID startAllNodes( String clusterName )
    {
        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkAllNodes( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new CheckClusterOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopAllNodes( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final ElasticsearchClusterConfiguration
                                                                 elasticsearchClusterConfiguration,
                                                         final TrackerOperation po )
    {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( elasticsearchClusterConfiguration, "Zookeeper cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new StandaloneSetupStrategy( environment, elasticsearchClusterConfiguration, po, this );
    }


    public EnvironmentBuildTask getDefaultEnvironmentBlueprint(
            ElasticsearchClusterConfiguration elasticsearchClusterConfiguration )
    {

        Preconditions.checkNotNull( elasticsearchClusterConfiguration, "Elasticsearch cluster config is null" );

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", ElasticsearchClusterConfiguration.PRODUCT_KEY,
                UUIDUtil.generateTimeBasedUUID() ) );

        // Node group
        NodeGroup nodesGroup = new NodeGroup();
        nodesGroup.setName( "DEFAULT" );
        nodesGroup.setNumberOfNodes( elasticsearchClusterConfiguration.getNumberOfNodes() );
        nodesGroup.setTemplateName( ElasticsearchClusterConfiguration.getTemplateName() );
        nodesGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodesGroup ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );
        return environmentBuildTask;
    }
}
