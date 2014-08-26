package org.safehaus.subutai.ui.flume;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.api.flume.Flume;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlumeUI implements PortalModule {

	public static final String MODULE_IMAGE = "flume.png";

	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Flume manager;
	private static Hadoop hadoopManager;
	private static ExecutorService executor;
	private static CommandRunner commandRunner;

	public FlumeUI(
			AgentManager agentManager,
			Tracker tracker,
			Flume manager,
			CommandRunner commandRunner,
			Hadoop hadoopManager) {
		FlumeUI.agentManager = agentManager;
		FlumeUI.tracker = tracker;
		FlumeUI.manager = manager;
		FlumeUI.commandRunner = commandRunner;
		FlumeUI.hadoopManager = hadoopManager;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Flume getManager() {
		return manager;
	}

	public static Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		agentManager = null;
		tracker = null;
		manager = null;
		hadoopManager = null;
		commandRunner = null;
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
		return FileUtil.getFile(FlumeUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new FlumeForm();
	}

}
