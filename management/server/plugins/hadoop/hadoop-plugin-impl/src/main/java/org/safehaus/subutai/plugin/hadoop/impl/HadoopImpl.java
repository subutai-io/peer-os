package org.safehaus.subutai.plugin.hadoop.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;
import org.safehaus.subutai.plugin.hadoop.impl.common.HadoopSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.handler.AddOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.BlockTaskTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.RestartJobTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StartJobTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StartTaskTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StatusJobTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StatusTaskTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StopJobTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.StopTaskTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker.UnblockTaskTrackerOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.BlockDataNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.ExcludeNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.IncludeNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.RestartNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StartDataNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StartNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StatusDataNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StatusNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StatusSecondaryNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StopDataNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StopNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.UnblockDataNodeOperationHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HadoopImpl implements Hadoop {
    public static final int INITIAL_CAPACITY = 2;

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ContainerManager containerManager;
    private NetworkManager networkManager;
    private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PluginDAO pluginDAO;


    public HadoopImpl(AgentManager agentManager, Tracker tracker, CommandRunner commandRunner, DbManager dbManager,
            NetworkManager networkManager, ContainerManager containerManager,
            EnvironmentManager environmentManager) {

        Preconditions.checkNotNull(commandRunner, "Command Runner is null");
        Preconditions.checkNotNull(agentManager, "Agent Manager is null");
        Preconditions.checkNotNull(dbManager, "Db Manager is null");
        Preconditions.checkNotNull(tracker, "Tracker is null");
        Preconditions.checkNotNull(containerManager, "Container manager is null");
        Preconditions.checkNotNull(environmentManager, "Environment manager is null");
        Preconditions.checkNotNull(networkManager, "Network manager is null");

        this.agentManager = agentManager;
        this.tracker = tracker;
        this.commandRunner = commandRunner;
        this.dbManager = dbManager;
        this.networkManager = networkManager;
        this.containerManager = containerManager;
        this.environmentManager = environmentManager;

        pluginDAO = new PluginDAO(dbManager);
        Commands.init(commandRunner);
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
    }


    @Override
    public UUID installCluster(final HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler = new InstallOperationHandler(this, hadoopClusterConfig);
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster(final String clusterName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterName), "Cluster name is null or empty");

        AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public List<HadoopClusterConfig> getClusters() {
        try {
            return pluginDAO.getInfo(HadoopClusterConfig.PRODUCT_KEY, HadoopClusterConfig.class);
        } catch (DBException e) {
            return Collections.emptyList();
        }
    }


    @Override
    public HadoopClusterConfig getCluster(String clusterName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterName), "Cluster name is null or empty");

        try {
            return pluginDAO.getInfo(HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class);
        } catch (DBException e) {
            return null;
        }
    }


    @Override
    public UUID startNameNode(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");


        AbstractOperationHandler operationHandler =
                new StartNameNodeOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNameNode(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");


        AbstractOperationHandler operationHandler =
                new StopNameNodeOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID restartNameNode(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");


        AbstractOperationHandler operationHandler =
                new RestartNameNodeOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusNameNode(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new StatusNameNodeOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusSecondaryNameNode(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new StatusSecondaryNameNodeOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }

    @Override
    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler =
                new StartDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler =
                new StopDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agent.getHostname()), "Lxc hostname is null or empty");

        AbstractOperationHandler operationHandler =
                new StatusDataNodeOperationHandler(this, hadoopClusterConfig.getClusterName(), agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startJobTracker(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new StartJobTrackerOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopJobTracker(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new StopJobTrackerOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID restartJobTracker(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new RestartJobTrackerOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusJobTracker(HadoopClusterConfig hadoopClusterConfig) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");

        AbstractOperationHandler operationHandler =
                new StatusJobTrackerOperationHandler(this, hadoopClusterConfig.getClusterName());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new StartTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new StopTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
        AbstractOperationHandler operationHandler =
                new StatusTaskTrackerOperationHandler(this, hadoopClusterConfig.getClusterName(),
                        agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName, int nodeCount ) {
        //    public UUID addNode( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new AddOperationHandler( this, clusterName, nodeCount );
        //        AbstractOperationHandler operationHandler = new AddOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName ) {
        return addNode( clusterName, 1 );
    }


    @Override
    public UUID blockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agent.getHostname()), "Lxc hostname is null or empty");

        AbstractOperationHandler operationHandler =
                new BlockDataNodeOperationHandler(this, hadoopClusterConfig.getClusterName(), agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }



    @Override
    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new DestroyNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }



    @Override
    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new ExcludeNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new IncludeNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID blockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agent.getHostname()), "Lxc hostname is null or empty");

        AbstractOperationHandler operationHandler =
                new BlockTaskTrackerOperationHandler(this, hadoopClusterConfig.getClusterName(), agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID unblockDataNode(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agent.getHostname()), "Lxc hostname is null or empty");

        AbstractOperationHandler operationHandler =
                new UnblockDataNodeOperationHandler(this, hadoopClusterConfig.getClusterName(), agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID unblockTaskTracker(HadoopClusterConfig hadoopClusterConfig, Agent agent) {
        Preconditions.checkNotNull(hadoopClusterConfig, "Configuration is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()),
                "Cluster name is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agent.getHostname()), "Lxc hostname is null or empty");

        AbstractOperationHandler operationHandler =
                new UnblockTaskTrackerOperationHandler(this, hadoopClusterConfig.getClusterName(),
                        agent.getHostname());
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(ProductOperation po,
            HadoopClusterConfig hadoopClusterConfig) {
        return new HadoopSetupStrategy(po, this, hadoopClusterConfig);
    }


    public ClusterSetupStrategy getClusterSetupStrategy(ProductOperation po, HadoopClusterConfig hadoopClusterConfig,
            Environment environment) {
        return new HadoopSetupStrategy(po, this, hadoopClusterConfig, environment);
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint(final HadoopClusterConfig config)
            throws ClusterSetupException {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName(String.format("%s-%s", HadoopClusterConfig.PRODUCT_KEY, UUID.randomUUID()));
        environmentBlueprint.setLinkHosts(true);
        environmentBlueprint.setExchangeSshKeys(true);
        environmentBlueprint.setDomainName(Common.DEFAULT_DOMAIN_NAME);
        Set<NodeGroup> nodeGroups = new HashSet<>(INITIAL_CAPACITY);

        //hadoop master nodes
        NodeGroup mastersGroup = new NodeGroup();
        mastersGroup.setName(NodeType.MASTER_NODE.name());
        mastersGroup.setNumberOfNodes(HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY);
        mastersGroup.setTemplateName(config.getTemplateName());
        mastersGroup.setPlacementStrategy(PlacementStrategy.MORE_RAM);
//        mastersGroup.setPhysicalNodes( convertAgent2Hostname() );
        nodeGroups.add(mastersGroup);

        //hadoop slave nodes
        NodeGroup slavesGroup = new NodeGroup();
        slavesGroup.setName(NodeType.SLAVE_NODE.name());
        slavesGroup.setNumberOfNodes(config.getCountOfSlaveNodes());
        slavesGroup.setTemplateName(config.getTemplateName());
        slavesGroup.setPlacementStrategy(PlacementStrategy.MORE_HDD);
//        slavesGroup.setPhysicalNodes( convertAgent2Hostname() );
        nodeGroups.add(slavesGroup);

        environmentBlueprint.setNodeGroups(nodeGroups);

        environmentBuildTask.setEnvironmentBlueprint(environmentBlueprint);
        environmentBuildTask.setPhysicalNodes(convertAgent2Hostname());


        return environmentBuildTask;
    }


    private Set<String> convertAgent2Hostname() throws ClusterSetupException {
        Set<Agent> agents = agentManager.getPhysicalAgents();

        if (agents != null && !agents.isEmpty()) {
            Set<String> hostNames = new HashSet<>(agents.size());

            for (Agent agent : agents) {
                hostNames.add(agent.getHostname());
            }

            return hostNames;
        } else {
            throw new ClusterSetupException("No physical machines available");
        }
    }
}
