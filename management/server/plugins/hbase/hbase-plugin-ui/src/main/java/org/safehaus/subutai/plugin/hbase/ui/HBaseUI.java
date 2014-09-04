/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author dilshat
 */
public class HBaseUI implements PortalModule {

	public static final String MODULE_IMAGE = "hbase.png";

	private static HBase hbaseManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public HBaseUI(
			AgentManager agentManager,
			Tracker tracker,
			HBase hbaseManager,
			CommandRunner commandRunner) {
		HBaseUI.agentManager = agentManager;
		HBaseUI.tracker = tracker;
		HBaseUI.hbaseManager = hbaseManager;
		HBaseUI.commandRunner = commandRunner;
	}


	public static Tracker getTracker() {
		return tracker;
	}


	public static HBase getHbaseManager() {
		return hbaseManager;
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
		hbaseManager = null;
		agentManager = null;
		tracker = null;
		commandRunner = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return HBaseClusterConfig.PRODUCT_KEY;
	}

	public String getName() {
		return HBaseClusterConfig.PRODUCT_KEY;
	}


	public Component createComponent() {
		return new HBaseForm();
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(HBaseUI.MODULE_IMAGE, this);
	}
}
