/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import com.vaadin.ui.Component;

/**
 * @author dilshat
 */
public class CassandraUI implements PortalModule {

	public static final String MODULE_IMAGE = "cassandra.png";

	private static Cassandra cassandraManager;
	private static AgentManager agentManager;
	private static CommandRunner commandRunner;
	private static Tracker tracker;
	private static ExecutorService executor;

	public CassandraUI(AgentManager agentManager, Cassandra cassandraManager, Tracker tracker, CommandRunner commandRunner) {
		CassandraUI.cassandraManager = cassandraManager;
		CassandraUI.agentManager = agentManager;
		CassandraUI.tracker = tracker;
		CassandraUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Cassandra getCassandraManager() {
		return cassandraManager;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		cassandraManager = null;
		agentManager = null;
		tracker = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return CassandraConfig.PRODUCT_KEY;
	}

	public String getName() {
		return CassandraConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(CassandraUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new CassandraForm();
	}

}
