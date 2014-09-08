package org.safehaus.subutai.impl.solr.handler.mock;


import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.solr.Commands;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.impl.solr.handler.AddNodeOperationHandler;
import org.safehaus.subutai.impl.solr.handler.InstallOperationHandler;
import org.safehaus.subutai.impl.solr.handler.UninstallOperationHandler;
import org.safehaus.subutai.product.common.test.unit.mock.CommandMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.LxcManagerMock;


public class MockBuilder {

	public static AbstractOperationHandler getInstallOperationWithResult(boolean success) {
		SolrImpl solrImpl = new SolrImplMock().setCommands(getCommands(success));
		Config config = new Config().setClusterName("test-cluster");

		return new InstallOperationHandler(solrImpl, config);
	}

	private static Commands getCommands(boolean installSuccess) {
		CommandMock installCommand = new CommandMock().setSucceeded(installSuccess);

		return new CommandsMock().setInstallCommand(installCommand);
	}

	public static AbstractOperationHandler getUninstallOperationWithResult(boolean success) {

		DbManager dbManager = new DbManagerMock().setDeleteInfoResult(success);

		SolrImpl solrImpl = new SolrImplMock()
				.setClusterConfig(new Config())
				.setDbManager(dbManager);

		return new UninstallOperationHandler(solrImpl, "test-cluster");
	}

	public static AbstractOperationHandler getAddNodeOperationWithResult(boolean success) {
		LxcManagerMock lxcManagerMock = new LxcManagerMock().setMockLxcMap(CommonMockBuilder.getLxcMap());

		SolrImpl solrImpl = new SolrImplMock()
				.setCommands(getCommands(success))
				.setClusterConfig(new Config())
				.setLxcManager(lxcManagerMock);

		return new AddNodeOperationHandler(solrImpl, "test-cluster");
	}
}
