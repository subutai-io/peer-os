package org.safehaus.subutai.plugin.flume.impl;

import java.util.*;
import java.util.concurrent.*;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.flume.api.*;
import org.safehaus.subutai.plugin.flume.impl.handler.*;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;

public class FlumeImpl extends FlumeBase implements Flume {

    public FlumeImpl() {
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
        pluginDao = new PluginDAO(dbManager);
    }

    public void destroy() {
        executor.shutdown();
    }

    @Override
    public UUID installCluster(final FlumeConfig config) {
        AbstractOperationHandler h = new InstallHandler(this, config);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID installCluster(FlumeConfig config, HadoopClusterConfig hadoopConfig) {
        InstallHandler h = new InstallHandler(this, config);
        h.setHadoopConfig(hadoopConfig);
        executor.execute(h);
        return h.getTrackerId();
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
    public UUID destroyNode(final String clusterName, final String hostname) {
        AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public List<FlumeConfig> getClusters() {
        try {
            return pluginDao.getInfo(FlumeConfig.PRODUCT_KEY, FlumeConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get clusters info", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public FlumeConfig getCluster(String clusterName) {
        try {
            return pluginDao.getInfo(FlumeConfig.PRODUCT_KEY, clusterName, FlumeConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get cluster info", ex);
            return null;
        }
    }

    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, FlumeConfig config, ProductOperation po) {
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            return new OverHadoopSetupStrategy(this, config, po);
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy(this, config, po);
            s.setEnvironment(env);
            return s;
        }
        return null;
    }

}
