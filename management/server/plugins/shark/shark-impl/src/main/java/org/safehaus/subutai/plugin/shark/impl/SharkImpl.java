/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.shark.impl;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.shark.api.Config;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.impl.handler.*;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SharkImpl implements Shark {

	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private Spark sparkManager;
	private DbManager dbManager;
	private Tracker tracker;
	private ExecutorService executor;

	public SharkImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker, Spark sparkManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.sparkManager = sparkManager;

		Commands.init(commandRunner);
	}

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public Spark getSparkManager() {
		return sparkManager;
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

	public UUID installCluster(final Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID uninstallCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID destroyNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	public UUID actualizeMasterIP(final String clusterName) {

		AbstractOperationHandler operationHandler = new ActualizeMasterIpOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

}
