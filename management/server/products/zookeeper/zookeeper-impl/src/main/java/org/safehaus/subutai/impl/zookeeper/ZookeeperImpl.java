package org.safehaus.subutai.impl.zookeeper;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.api.zookeeper.Zookeeper;
import org.safehaus.subutai.impl.zookeeper.handler.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;


public class ZookeeperImpl implements Zookeeper {

	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private LxcManager lxcManager;
	private NetworkManager networkManager;
	private ExecutorService executor;


	public ZookeeperImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
	                     LxcManager lxcManager, NetworkManager networkManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.lxcManager = lxcManager;
		this.networkManager = networkManager;

		Commands.init(commandRunner);
	}


	public NetworkManager getNetworkManager() {
		return networkManager;
	}


	public CommandRunner getCommandRunner() {
		return commandRunner;
	}


	public AgentManager getAgentManager() {
		return agentManager;
	}


	public DbManager getDbManager() {
		return dbManager;
	}


	public Tracker getTracker() {
		return tracker;
	}


	public LxcManager getLxcManager() {
		return lxcManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	public UUID installCluster(Config config) {

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID uninstallCluster(String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public List<Config> getClusters() {

		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	public UUID startNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID stopNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID checkNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(String clusterName) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(String clusterName, String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID destroyNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID addProperty(String clusterName, String fileName, String propertyName, String propertyValue) {

		AbstractOperationHandler operationHandler =
				new AddPropertyOperationHandler(this, clusterName, fileName, propertyName, propertyValue);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID removeProperty(String clusterName, String fileName, String propertyName) {

		AbstractOperationHandler operationHandler =
				new RemovePropertyOperationHandler(this, clusterName, fileName, propertyName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

    @Override
    public UUID install( String hostName ) {
        AbstractOperationHandler h = new SingleInstallation( this, hostName );
        executor.execute( h );
        return h.getTrackerId();
    }

    @Override
    public UUID start(String hostName) {
        AbstractOperationHandler h = new StartStandalone(this, hostName);
        executor.execute(h);
        return h.getTrackerId();
    }

}
