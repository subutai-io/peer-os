package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.elasticsearch.api.*;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.network.NetworkManager;
import org.safehaus.subutai.api.tracker.Tracker;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class ElasticsearchImpl implements Elasticsearch {

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

    public PluginDAO getPluginDAO() {
        return pluginDAO;
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


    public DbManager getDbManager() {
        return dbManager;
    }


    public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager ) {
        this.containerManager = containerManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void init() {
		Commands.init( commandRunner );
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
	}

	public void setDbManager(DbManager dbManager) {
		this.dbManager = dbManager;

	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}


	public UUID installCluster(final Config config) {

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
	}


	@Override
	public UUID uninstallCluster(final String clusterName) {
		final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));

		executor.execute(new Runnable() {

			public void run() {
				Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
					return;
				}

				po.addLog("Destroying lxc containers...");

				try {
					lxcManager.destroyLxcs(config.getNodes());
					po.addLog("Lxc containers successfully destroyed");
				} catch (LxcDestroyException ex) {
					po.addLog(String.format("%s, skipping...", ex.getMessage()));
				}
				po.addLog("Updating db...");
				if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
					po.addLogDone("Cluster info deleted from DB\nDone");
				} else {
					po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
				}

			}
		});

		return po.getId();
	}

	@Override
	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	@Override
	public UUID startAllNodes(final String clusterName) {
		final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
				String.format("Starting cluster %s", clusterName));

		executor.execute(new Runnable() {
			public void run() {
				Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
				if (config == null) {
					po.addLogFailed(
							String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
					return;
				}
				Command startServiceCommand = Commands.getStartCommand(config.getNodes());
				commandRunner.runCommand(startServiceCommand);

				if (startServiceCommand.hasSucceeded()) {
					po.addLogDone("Start succeeded");
				} else {
					po.addLogFailed(String.format("Start failed, %s", startServiceCommand.getAllErrors()));
				}
			}
		});

		return po.getId();
	}

	@Override
	public UUID checkAllNodes(final String clusterName) {
		final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
				String.format("Checking cluster %s", clusterName));

		executor.execute(new Runnable() {

			public void run() {
				Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
				if (config == null) {
					po.addLogFailed(
							String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
					return;
				}

				Command checkStatusCommand = Commands.getStatusCommand(config.getNodes());
				commandRunner.runCommand(checkStatusCommand);

				if (checkStatusCommand.hasSucceeded()) {
					po.addLogDone("All nodes are running.");
				} else {
					logStatusResults(po, checkStatusCommand);
				}
			}
		});

		return po.getId();
	}

	@Override
	public UUID stopAllNodes(final String clusterName) {
		final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
				String.format("Stopping cluster %s", clusterName));

		executor.execute(new Runnable() {

			public void run() {
				Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
				if (config == null) {
					po.addLogFailed(
							String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
					return;
				}

				Command stopServiceCommand = Commands.getStopCommand(config.getNodes());
				commandRunner.runCommand(stopServiceCommand);

				if (stopServiceCommand.hasSucceeded()) {
					po.addLogDone("Stop succeeded");
				} else {
					po.addLogFailed(String.format("Start failed, %s", stopServiceCommand.getAllErrors()));
				}
			}
		});

		return po.getId();
	}

	private void logStatusResults(ProductOperation po, Command checkStatusCommand) {

		String log = "";

		for (Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet()) {

			String status = "UNKNOWN";
			if (e.getValue().getExitCode() == 0) {
				status = "RUNNING";
			} else if (e.getValue().getExitCode() == 768) {
				status = "NOT RUNNING";
			}

			log += String.format("- %s: %s\n", e.getValue().getAgentUUID(), status);
		}

		po.addLogDone(log);
	}


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( Config config ) {

        Preconditions.checkNotNull( config, "Elasticsearch cluster config is null" );

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", Config.PRODUCT_KEY, UUID.randomUUID() ) );

        // Node group
        NodeGroup nodesGroup = new NodeGroup();
        nodesGroup.setName( "DEFAULT" );
        nodesGroup.setNumberOfNodes( config.getNumberOfNodes() );
        nodesGroup.setTemplateName( config.getTemplateName() );
        nodesGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodesGroup ) );

        return environmentBlueprint;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final Config config,
                                                         final ProductOperation po ) {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new StandaloneSetupStrategy( environment, config, po, this );
    }
}
