package org.safehaus.subutai.impl.oozie;


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
import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.impl.oozie.handler.CheckServerOperationHandler;
import org.safehaus.subutai.impl.oozie.handler.InstallOperationHandler;
import org.safehaus.subutai.impl.oozie.handler.StartServerOperationHandler;
import org.safehaus.subutai.impl.oozie.handler.StopServerOperationHandler;
import org.safehaus.subutai.impl.oozie.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.NodeGroup;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.collect.Sets;


public class OozieImpl implements Oozie {

    public AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;
    private LxcManager lxcManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;


    public OozieImpl( final AgentManager agentManager, final DbManager dbManager, final Tracker tracker,
                      final ExecutorService executor, final CommandRunner commandRunner, final LxcManager lxcManager,
                      final EnvironmentManager environmentManager, final ContainerManager containerManager ) {
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
//        this.executor = executor;
        this.commandRunner = commandRunner;
        this.lxcManager = lxcManager;
        this.environmentManager = environmentManager;
        this.containerManager = containerManager;

        Commands.init( commandRunner );
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


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
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


    public void init() {
        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public void setDbManager( DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public UUID installCluster( final OozieConfig config ) {

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
        //        return po.getId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<OozieConfig> getClusters() {

        return dbManager.getInfo( OozieConfig.PRODUCT_KEY, OozieConfig.class );
    }


    @Override
    public OozieConfig getCluster( String clusterName ) {
        return dbManager.getInfo( OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class );
    }


    @Override
    public UUID startServer( final OozieConfig config ) {
        AbstractOperationHandler operationHandler = new StartServerOperationHandler( this, config.getClusterName() );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopServer( final OozieConfig config ) {
        AbstractOperationHandler operationHandler = new StopServerOperationHandler( this, config.getClusterName() );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkServerStatus( final OozieConfig config ) {

        AbstractOperationHandler operationHandler = new CheckServerOperationHandler( this, config.getClusterName() );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final OozieConfig config,
                                                         final ProductOperation po ) {
        return new OozieSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final OozieConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        int numberOfNodes = config.getClients().size() + 1; // +1 server
        nodeGroup.setNumberOfNodes( numberOfNodes );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return environmentBlueprint;
    }
}
