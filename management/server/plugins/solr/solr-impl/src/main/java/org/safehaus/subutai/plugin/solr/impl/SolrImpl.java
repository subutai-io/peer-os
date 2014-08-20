package org.safehaus.subutai.plugin.solr.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.NodeGroup;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class SolrImpl implements Solr {

    protected Commands commands;
    private CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected DbManager dbManager;
    private Tracker tracker;
    protected LxcManager lxcManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private ExecutorService executor;


    public SolrImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                     LxcManager lxcManager, EnvironmentManager environmentManager, ContainerManager containerManager ) {
        this.commands = new Commands( commandRunner );
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.lxcManager = lxcManager;
        this.environmentManager = environmentManager;
        this.containerManager = containerManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public Commands getCommands() {
        return commands;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public LxcManager getLxcManager() {
        return lxcManager;
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( SolrClusterConfig.PRODUCT_KEY, clusterName, SolrClusterConfig.class );
    }


    public List<SolrClusterConfig> getClusters() {
        return dbManager.getInfo( SolrClusterConfig.PRODUCT_KEY, SolrClusterConfig.class );
    }


    public UUID installCluster( final SolrClusterConfig solrClusterConfig ) {

        Preconditions.checkNotNull( solrClusterConfig, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, solrClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( final String clusterName, final String lxcHostName ) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostName ) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName ) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName ) {

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName ) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final SolrClusterConfig config,
                                                         final ProductOperation po ) {
        return new SolrSetupStrategy( this, po, config, environment );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SolrClusterConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", SolrClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );

        //1 node group
        NodeGroup solrGroup = new NodeGroup();
        solrGroup.setName( "DEFAULT" );
        solrGroup.setNumberOfNodes( config.getNumberOfNodes() );
        solrGroup.setTemplateName( config.getTemplateName() );
        solrGroup.setPlacementStrategy( SolrSetupStrategy.getPlacementStrategy() );


        environmentBlueprint.setNodeGroups( Sets.newHashSet( solrGroup ) );

        return environmentBlueprint;
    }
}
