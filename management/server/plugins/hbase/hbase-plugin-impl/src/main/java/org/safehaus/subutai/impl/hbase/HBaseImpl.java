package org.safehaus.subutai.impl.hbase;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.impl.hbase.handler.CheckClusterOperationHandler;
import org.safehaus.subutai.impl.hbase.handler.InstallOperationHandler;
import org.safehaus.subutai.impl.hbase.handler.StartClusterOperationHandler;
import org.safehaus.subutai.impl.hbase.handler.StopClusterOperationHandler;
import org.safehaus.subutai.impl.hbase.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;

import com.google.common.base.Preconditions;


public class HBaseImpl implements HBase {

    private AgentManager agentManager;
    private Hadoop hadoopManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;


    public HBaseImpl( AgentManager agentManager,
                      Hadoop hadoopManager,
                      DbManager dbManager,
                      Tracker tracker,
                      CommandRunner commandRunner,
                      EnvironmentManager environmentManager,
                      ContainerManager containerManager ) {
        this.agentManager = agentManager;
        this.hadoopManager = hadoopManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        //        this.executor = executor;
        this.commandRunner = commandRunner;
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


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
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


    public UUID installCluster( final HBaseConfig config ) {
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        //        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<HBaseConfig> getClusters() {

        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, HBaseConfig.class );
    }


    @Override
    public List<Config> getHadoopClusters() {
        return hadoopManager.getClusters();
    }


    @Override
    public Config getHadoopCluster( String clusterName ) {
        return hadoopManager.getCluster( clusterName );
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final HBaseConfig config,
                                                         final ProductOperation po ) {
        return new HBaseSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final HBaseConfig config ) {
        return null;
    }


    @Override
    public HBaseConfig getCluster( String clusterName ) {
        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
    }


    @Override
    public UUID startCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new CheckClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    private Set<Agent> getAllNodes( HBaseConfig config ) throws Exception {
        final Set<Agent> allNodes = new HashSet<>();

        if ( agentManager.getAgentByHostname( config.getMaster() ) == null ) {
            throw new Exception( String.format( "Master node %s not connected", config.getMaster() ) );
        }
        allNodes.add( agentManager.getAgentByHostname( config.getMaster() ) );
        if ( agentManager.getAgentByHostname( config.getBackupMasters() ) == null ) {
            throw new Exception( String.format( "Backup master node %s not connected", config.getBackupMasters() ) );
        }
        allNodes.add( agentManager.getAgentByHostname( config.getBackupMasters() ) );

        for ( String hostname : config.getRegion() ) {
            if ( agentManager.getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Region server node %s not connected", hostname ) );
            }
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        for ( String hostname : config.getQuorum() ) {
            if ( agentManager.getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Quorum node %s not connected", hostname ) );
            }
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        return allNodes;
    }
}
