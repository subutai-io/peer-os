package org.safehaus.subutai.plugin.flume.impl;

import java.util.*;
import java.util.concurrent.*;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.handler.*;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

public class FlumeImpl extends FlumeBase implements Flume {

    public FlumeImpl() {
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
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
