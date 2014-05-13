package org.safehaus.kiskis.mgmt.impl.flume;

import java.util.*;
import java.util.concurrent.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.*;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.flume.Flume;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.impl.flume.handler.*;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

public class FlumeImpl implements Flume {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private DbManager dbManager;

    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public UUID installCluster(final Config config) {
        AbstractOperationHandler h = new InstallHandler(this, config);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID uninstallCluster(final String clusterName) {
        AbstractOperationHandler h = new UninstallHandler(this, clusterName);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID startNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID stopNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID checkNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID addNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID destroyNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

}
