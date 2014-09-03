/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class MongoUI implements PortalModule {

	public static final String MODULE_IMAGE = "mongodb.png";

	private static Mongo mongoManager;
	private static AgentManager agentManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;
	private static Tracker tracker;

	public MongoUI(AgentManager agentManager, Mongo mongoManager, Tracker tracker, CommandRunner commandRunner) {
		MongoUI.agentManager = agentManager;
		MongoUI.mongoManager = mongoManager;
		MongoUI.tracker = tracker;
		MongoUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Mongo getMongoManager() {
		return mongoManager;
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
		tracker = null;
		mongoManager = null;
		agentManager = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return MongoClusterConfig.PRODUCT_KEY;
	}

	public String getName() {
		return MongoClusterConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(MongoUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new MongoForm();
	}

}
