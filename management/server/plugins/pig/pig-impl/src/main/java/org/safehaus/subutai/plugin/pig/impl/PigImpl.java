package org.safehaus.subutai.plugin.pig.impl;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.pig.impl.handler.InstallOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PigImpl implements Pig {

	protected Commands commands;
	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private Hadoop hadoopManager;
    private PluginDAO pluginDao;

	public PigImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
		this.commands = new Commands(commandRunner);
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
        pluginDao = new PluginDAO(dbManager);
	}

    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    public ContainerManager getContainerManager() {
        return containerManager;
    }

    public void setContainerManager(ContainerManager containerManager) {
        this.containerManager = containerManager;
    }

    public Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        this.hadoopManager = hadoopManager;
    }

	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	public Commands getCommands() {
		return commands;
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


	public CommandRunner getCommandRunner() {
		return commandRunner;
	}


    public PluginDAO getPluginDao() {
        return pluginDao;
    }


    @Override
	public UUID installCluster(Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);
		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

    @Override
    public UUID installCluster(Config config, HadoopClusterConfig hadoopConfig) {
        InstallOperationHandler operationHandler = new InstallOperationHandler(this, config);
        operationHandler.setHadoopConfig( hadoopConfig );
        executor.execute(operationHandler);
        return operationHandler.getTrackerId();
    }


	@Override
	public UUID uninstallCluster(final String clusterName) {
		return null;
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
	public UUID destroyNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, Config config, ProductOperation po ) {
        if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            return new OverHadoopSetupStrategy( this, config, po );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy(this, config, po);
            s.setEnvironment(env);
            return s;
        }
        return null;
    }
}
