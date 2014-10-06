package org.safehaus.subutai.plugin.solr.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.UninstallOperationHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class SolrImpl implements Solr
{

    protected Commands commands;
    protected AgentManager agentManager;
    private CommandRunner commandRunner;
    private Tracker tracker;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;


    public SolrImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                     EnvironmentManager environmentManager, ContainerManager containerManager )
    {

        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );
        Preconditions.checkNotNull( containerManager, "Container manager is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.commands = new Commands( commandRunner );
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
        this.containerManager = containerManager;
        this.pluginDAO = new PluginDAO( dbManager );
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public UUID installCluster( final SolrClusterConfig solrClusterConfig )
    {

        Preconditions.checkNotNull( solrClusterConfig, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, solrClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<SolrClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( SolrClusterConfig.PRODUCT_KEY, SolrClusterConfig.class );
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        return pluginDAO.getInfo( SolrClusterConfig.PRODUCT_KEY, clusterName, SolrClusterConfig.class );
    }


    public UUID startNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final SolrClusterConfig config,
                                                         final ProductOperation po )
    {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Solr cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new SolrSetupStrategy( this, po, config, environment );
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( SolrClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Solr cluster config is null" );

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", SolrClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );

        //1 node group
        NodeGroup solrGroup = new NodeGroup();
        solrGroup.setName( "DEFAULT" );
        solrGroup.setNumberOfNodes( config.getNumberOfNodes() );
        solrGroup.setTemplateName( config.getTemplateName() );
        solrGroup.setPlacementStrategy( SolrSetupStrategy.getPlacementStrategy() );


        environmentBlueprint.setNodeGroups( Sets.newHashSet( solrGroup ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );


        return environmentBuildTask;
    }
}
