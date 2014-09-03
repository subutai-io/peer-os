/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.shark.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.shark.api.Config;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SharkUI implements PortalModule {

	public static final String MODULE_IMAGE = "shark.png";

	private static Shark sharkManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Spark sparkManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public SharkUI(AgentManager agentManager, Tracker tracker, Spark sparkManager, Shark sharkManager, CommandRunner commandRunner) {
		SharkUI.agentManager = agentManager;
		SharkUI.tracker = tracker;
		SharkUI.sparkManager = sparkManager;
		SharkUI.sharkManager = sharkManager;
		SharkUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Shark getSharkManager() {
		return sharkManager;
	}

	public static Spark getSparkManager() {
		return sparkManager;
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
		sharkManager = null;
		agentManager = null;
		sparkManager = null;
		tracker = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return Config.PRODUCT_KEY;
	}

	public String getName() {
		return Config.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(SharkUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new SharkForm();
	}

}
