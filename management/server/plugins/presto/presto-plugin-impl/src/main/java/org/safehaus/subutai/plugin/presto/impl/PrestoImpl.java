/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.presto.impl;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.impl.handler.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PrestoImpl implements Presto {

	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private ExecutorService executor;

	public PrestoImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;

		Commands.init(commandRunner);
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

	public UUID installCluster(final PrestoClusterConfig config) {

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

	public List<PrestoClusterConfig> getClusters() {
		return dbManager.getInfo(PrestoClusterConfig.PRODUCT_KEY, PrestoClusterConfig.class);
	}

	@Override
	public PrestoClusterConfig getCluster(String clusterName) {
		return dbManager.getInfo(PrestoClusterConfig.PRODUCT_KEY, clusterName, PrestoClusterConfig.class);
	}

	public UUID addWorkerNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddWorkerNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID destroyWorkerNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyWorkerNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID changeCoordinatorNode(final String clusterName, final String newCoordinatorHostname) {

		AbstractOperationHandler operationHandler = new ChangeCoordinatorNodeOperationHandler(this, clusterName, newCoordinatorHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID startNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();

	}

	public UUID stopNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID checkNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

}
