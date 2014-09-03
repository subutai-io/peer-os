/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.presto;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.presto.Config;
import org.safehaus.subutai.api.presto.Presto;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class PrestoUI implements PortalModule {

	public static final String MODULE_IMAGE = "presto.png";

	private static Presto prestoManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public PrestoUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Presto prestoManager, CommandRunner commandRunner) {
		PrestoUI.agentManager = agentManager;
		PrestoUI.tracker = tracker;
		PrestoUI.hadoopManager = hadoopManager;
		PrestoUI.prestoManager = prestoManager;
		PrestoUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Presto getPrestoManager() {
		return prestoManager;
	}

	public static Hadoop getHadoopManager() {
		return hadoopManager;
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
		prestoManager = null;
		agentManager = null;
		hadoopManager = null;
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
		return FileUtil.getFile(PrestoUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new PrestoForm();
	}

}
