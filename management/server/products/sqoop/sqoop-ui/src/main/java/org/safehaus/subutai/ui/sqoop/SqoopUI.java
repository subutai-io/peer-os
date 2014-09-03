package org.safehaus.subutai.ui.sqoop;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.api.sqoop.Sqoop;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqoopUI implements PortalModule {

	public static final String MODULE_IMAGE = "sqoop.png";

	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Sqoop manager;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;

	private static ExecutorService executor;
	private static SqoopForm form;

	public SqoopUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Sqoop manager, CommandRunner commandRunner) {
		SqoopUI.agentManager = agentManager;
		SqoopUI.tracker = tracker;
		SqoopUI.hadoopManager = hadoopManager;
		SqoopUI.manager = manager;
		SqoopUI.commandRunner = commandRunner;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Sqoop getManager() {
		return manager;
	}

	public void setManager(Sqoop manager) {
		SqoopUI.manager = manager;
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

	public static SqoopForm getForm() {
		return form;
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
		return FileUtil.getFile(SqoopUI.MODULE_IMAGE, this);
	}


	public Component createComponent() {
		SqoopUI.form = new SqoopForm();
		return SqoopUI.form;
	}

}
