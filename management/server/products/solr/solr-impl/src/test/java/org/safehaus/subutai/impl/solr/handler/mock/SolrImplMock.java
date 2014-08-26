package org.safehaus.subutai.impl.solr.handler.mock;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.impl.solr.Commands;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.product.common.test.unit.mock.*;


public class SolrImplMock extends SolrImpl {

	private Config clusterConfig = null;

	public SolrImplMock() {
		super(new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
				new LxcManagerMock());
	}


	public SolrImplMock setCommands(Commands commands) {
		this.commands = commands;
		return this;
	}


	public SolrImplMock setClusterConfig(Config clusterConfig) {
		this.clusterConfig = clusterConfig;
		return this;
	}


	@Override
	public Config getCluster(String clusterName) {
		return clusterConfig;
	}


	public SolrImplMock setDbManager(DbManager dbManager) {
		this.dbManager = dbManager;
		return this;
	}


	public SolrImplMock setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
		return this;
	}


	public SolrImplMock setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
		return this;
	}

}
