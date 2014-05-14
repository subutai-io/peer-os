/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.pig;

import com.google.common.base.Preconditions;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.pig.Config;
import org.safehaus.kiskis.mgmt.api.pig.Pig;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.impl.pig.handler.AddNodeOperationHandler;
import org.safehaus.kiskis.mgmt.impl.pig.handler.DestroyNodeOperationHandler;
import org.safehaus.kiskis.mgmt.impl.pig.handler.InstallOperationHandler;
import org.safehaus.kiskis.mgmt.impl.pig.handler.UninstallOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class PigImpl implements Pig {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;


    public PigImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;

        Commands.init(commandRunner);
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
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

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public UUID installCluster(final Config config) {

        Preconditions.checkNotNull(config, "Configuration is null");

        AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);
        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID uninstallCluster(final String clusterName) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);
        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID addNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

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
