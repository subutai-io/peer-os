/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mahout;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.mahout.Config;
import org.safehaus.subutai.api.mahout.Mahout;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class MahoutUI implements PortalModule {

	public static final String MODULE_IMAGE = "mahout.png";

	private static Mahout mahoutManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public MahoutUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Mahout mahoutManager, CommandRunner commandRunner) {
		MahoutUI.agentManager = agentManager;
		MahoutUI.tracker = tracker;
		MahoutUI.hadoopManager = hadoopManager;
		MahoutUI.mahoutManager = mahoutManager;
		MahoutUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Mahout getMahoutManager() {
		return mahoutManager;
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
		mahoutManager = null;
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
		return FileUtil.getFile(MahoutUI.MODULE_IMAGE, this);
	}


	public Component createComponent() {
		return new MahoutForm();
	}

}
