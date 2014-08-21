package org.safehaus.subutai.plugin.flume.impl;

import org.safehaus.subutai.plugin.flume.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.UninstallHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.StatusHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.StartHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.StopHandler;
import java.util.*;
import java.util.concurrent.*;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;

public class FlumeImpl implements Flume {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private DbManager dbManager;

    private ExecutorService executor;

    public FlumeImpl(CommandRunner commandRunner, AgentManager agentManager, Tracker tracker, DbManager dbManager) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.dbManager = dbManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    @Override
    public UUID installCluster(final FlumeConfig config) {
        AbstractOperationHandler h = new InstallHandler(this, config);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID installCluster(FlumeConfig config, HadoopClusterConfig hadoopConfig) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UUID uninstallCluster(final String clusterName) {
        AbstractOperationHandler h = new UninstallHandler(this, clusterName);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID startNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID stopNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID checkNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID addNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID addNode(String clusterName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UUID destroyNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public List<FlumeConfig> getClusters() {
        return dbManager.getInfo(FlumeConfig.PRODUCT_KEY, FlumeConfig.class);
    }

    @Override
    public FlumeConfig getCluster(String clusterName) {
        return dbManager.getInfo(FlumeConfig.PRODUCT_KEY, clusterName, FlumeConfig.class);
    }

}
