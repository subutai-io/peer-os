package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class ElasticsearchImpl implements Elasticsearch
{
    private static final Logger LOG = Logger.getLogger( ElasticsearchImpl.class.getName() );
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private PluginDAO pluginDAO;
    private ContainerManager containerManager;
    private EnvironmentManager environmentManager;


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( CommandRunner commandRunner )
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


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
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


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void init()
    {
        Commands.init( commandRunner );
        this.pluginDAO = new PluginDAO( dbManager );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public void setLxcManager( LxcManager lxcManager )
    {
        this.lxcManager = lxcManager;
    }


    public void setNetworkManager( NetworkManager networkManager )
    {
        this.networkManager = networkManager;
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
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );
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
                                                         final ProductOperation po )
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
        environmentBlueprint
                .setName( String.format( "%s-%s", ElasticsearchClusterConfiguration.PRODUCT_KEY, UUID.randomUUID() ) );

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
