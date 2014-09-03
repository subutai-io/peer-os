/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import com.vaadin.ui.Component;

/**
 * @author dilshat
 */
public class OozieUI implements PortalModule {

	public static final String MODULE_IMAGE = "oozie.png";

	private static Oozie oozieManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public OozieUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Oozie oozieManager, CommandRunner commandRunner) {
		OozieUI.agentManager = agentManager;
		OozieUI.tracker = tracker;
		OozieUI.hadoopManager = hadoopManager;
		OozieUI.oozieManager = oozieManager;
		OozieUI.commandRunner = commandRunner;
	}

	public static Oozie getOozieManager() {
		return oozieManager;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		oozieManager = null;
		agentManager = null;
		tracker = null;
		hadoopManager = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return OozieConfig.PRODUCT_KEY;
	}

	public String getName() {
		return OozieConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(OozieUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new OozieForm();
	}

}
