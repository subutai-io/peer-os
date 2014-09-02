/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.spark.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SparkUI implements PortalModule {

	public static final String MODULE_IMAGE = "spark.png";

	private static Spark sparkManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public SparkUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Spark sparkManager, CommandRunner commandRunner) {
		SparkUI.agentManager = agentManager;
		SparkUI.tracker = tracker;
		SparkUI.hadoopManager = hadoopManager;
		SparkUI.sparkManager = sparkManager;
		SparkUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Spark getSparkManager() {
		return sparkManager;
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
		sparkManager = null;
		agentManager = null;
		hadoopManager = null;
		tracker = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return SparkClusterConfig.PRODUCT_KEY;
	}

	public String getName() {
		return SparkClusterConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(SparkUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new SparkForm();
	}

}
