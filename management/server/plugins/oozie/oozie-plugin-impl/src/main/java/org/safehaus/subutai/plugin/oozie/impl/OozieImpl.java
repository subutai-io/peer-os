package org.safehaus.subutai.plugin.oozie.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.handler.CheckServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.StartServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.StopServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.UninstallHandler;

import com.google.common.collect.Sets;


public class OozieImpl extends OozieBase {

    PluginDAO pluginDAO;
    Commands commands;
    AgentManager agentManager;
    DbManager dbManager;
    Tracker tracker;
    CommandRunner commandRunner;
    LxcManager lxcManager;
    EnvironmentManager environmentManager;
    ContainerManager containerManager;
    Hadoop hadoopManager;
    ExecutorService executor;


    public OozieImpl() {

    }


    public void init() {
        this.pluginDAO = new PluginDAO( dbManager );
        this.commands = new Commands( commandRunner );

        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO ) {
        this.pluginDAO = pluginDAO;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( final Tracker tracker ) {
        this.tracker = tracker;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public LxcManager getLxcManager() {
        return lxcManager;
    }


    public void setLxcManager( final LxcManager lxcManager ) {
        this.lxcManager = lxcManager;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager ) {
        this.containerManager = containerManager;
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }


    public void destroy() {
        agentManager = null;
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }


    public UUID installCluster( final OozieClusterConfig config ) {
        AbstractOperationHandler operationHandler = new InstallHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new UninstallHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<OozieClusterConfig> getClusters() {
        return dbManager.getInfo( OozieClusterConfig.PRODUCT_KEY, OozieClusterConfig.class );
    }


    @Override
    public OozieClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName, OozieClusterConfig.class );
    }


    @Override
    public UUID startServer( final OozieClusterConfig config ) {
        AbstractOperationHandler operationHandler = new StartServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopServer( final OozieClusterConfig config ) {
        AbstractOperationHandler operationHandler = new StopServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkServerStatus( final OozieClusterConfig config ) {
        AbstractOperationHandler operationHandler = new CheckServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final OozieClusterConfig config,
                                                         final ProductOperation po ) {

        if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            return new OverHadoopSetupStrategy( this, po, config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy( this, po, config );
            s.setEnvironment( environment );
            return s;
        }
        return null;

        //        return new OozieSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final OozieClusterConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup oozieGroup = new NodeGroup();
        oozieGroup.setTemplateName( config.getTemplateNameClient() );
        oozieGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        int numberOfNodes = config.getClients().size();
        oozieGroup.setNumberOfNodes( numberOfNodes );

        NodeGroup oozieServer = new NodeGroup();
        oozieServer.setTemplateName( config.getTemplateNameServer() );
        oozieServer.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        oozieServer.setNumberOfNodes( 1 );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( oozieGroup ) );
        environmentBlueprint.setNodeGroups( Sets.newHashSet( oozieServer ) );

        return environmentBlueprint;
    }


    @Override
    public UUID addNode( final String clustername, final String lxchostname, final String nodetype ) {
        return null;
    }


    @Override
    public UUID destroyNode( final String clustername, final String lxchostname, final String nodetype ) {
        return null;
    }
}
