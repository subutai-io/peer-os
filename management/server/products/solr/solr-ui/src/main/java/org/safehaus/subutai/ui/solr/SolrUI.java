/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.solr;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.api.solr.Solr;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SolrUI implements PortalModule {

	public static final String MODULE_IMAGE = "solr.png";

	private static Solr solrManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public SolrUI(AgentManager agentManager, Tracker tracker, Solr solrManager, CommandRunner commandRunner) {
		SolrUI.agentManager = agentManager;
		SolrUI.tracker = tracker;
		SolrUI.solrManager = solrManager;
		SolrUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Solr getSolrManager() {
		return solrManager;
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
		solrManager = null;
		agentManager = null;
		tracker = null;
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
		return FileUtil.getFile(SolrUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new SolrForm();
	}

}
