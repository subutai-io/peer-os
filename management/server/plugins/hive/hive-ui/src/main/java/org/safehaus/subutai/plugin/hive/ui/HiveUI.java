package org.safehaus.subutai.plugin.hive.ui;

import com.vaadin.ui.Component;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.server.ui.api.PortalModule;

public class HiveUI implements PortalModule {

	public static final String MODULE_IMAGE = "hive.png";

	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hive manager;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public HiveUI(
			AgentManager agentManager,
			Tracker tracker,
			Hive manager,
			Hadoop hadoopManager,
			CommandRunner commandRunner
	) {
		HiveUI.agentManager = agentManager;
		HiveUI.tracker = tracker;
		HiveUI.manager = manager;
		HiveUI.hadoopManager = hadoopManager;
		HiveUI.commandRunner = commandRunner;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Hive getManager() {
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
		executor.shutdown();
	}

	@Override
	public String getId() {
		return HiveConfig.PRODUCT_KEY;
	}

	public String getName() {
		return HiveConfig.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(HiveUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new HiveForm();
	}

}
