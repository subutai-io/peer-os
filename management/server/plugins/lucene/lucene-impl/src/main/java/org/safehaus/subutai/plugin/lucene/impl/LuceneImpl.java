package org.safehaus.subutai.plugin.lucene.impl;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.lucene.api.Config;
import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.lucene.api.SetupType;
import org.safehaus.subutai.plugin.lucene.impl.handler.*;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LuceneImpl implements Lucene {

	protected Commands commands;
	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private Hadoop hadoopManager;
	private ExecutorService executor;
    PluginDAO pluginDao;
    EnvironmentManager environmentManager;
    ContainerManager containerManager;


	public LuceneImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
	                  Hadoop hadoopManager, EnvironmentManager environmentManager,  ContainerManager containerManager) {
		this.commands = new Commands(commandRunner);
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.hadoopManager = hadoopManager;
        this.environmentManager = environmentManager;
        this.containerManager = containerManager;
        pluginDao = new PluginDAO(dbManager);
	}


	public Hadoop getHadoopManager() {
		return hadoopManager;
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


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	@Override
	public UUID installCluster(final Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, Config config, ProductOperation po) {
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            return new OverHadoopSetupStrategy(this, config, po);
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
//            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy(this, config, po);
//            s.setEnvironment(env);
//            return s;
        }
        return null;
    }


	@Override
	public UUID uninstallCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
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
	public UUID addNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID destroyNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


}
