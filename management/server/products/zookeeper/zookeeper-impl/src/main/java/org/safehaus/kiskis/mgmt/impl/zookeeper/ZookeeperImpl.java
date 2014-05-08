package org.safehaus.kiskis.mgmt.impl.zookeeper;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.api.zookeeper.Zookeeper;
import org.safehaus.kiskis.mgmt.impl.zookeeper.handler.*;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZookeeperImpl implements Zookeeper {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;

    public ZookeeperImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker, LxcManager lxcManager) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.lxcManager = lxcManager;

        Commands.init(commandRunner);
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


    public UUID installCluster(final Config config) {

        AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID uninstallCluster(final String clusterName) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID startNode(final String clusterName, final String lxcHostName) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostName) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostName) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostName) {

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID addProperty(final String clusterName, final String fileName, final String propertyName, final String propertyValue) {

        AbstractOperationHandler operationHandler = new AddPropertyOperationHandler(this, clusterName, fileName, propertyName, propertyValue);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID removeProperty(final String clusterName, final String fileName, final String propertyName) {

        AbstractOperationHandler operationHandler = new RemovePropertyOperationHandler(this, clusterName, fileName, propertyName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID addNode(final String clusterName) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName);

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

}
