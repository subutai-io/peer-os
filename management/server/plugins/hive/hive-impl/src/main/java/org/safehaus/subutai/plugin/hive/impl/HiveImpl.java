package org.safehaus.subutai.plugin.hive.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.impl.handler.*;

public class HiveImpl extends HiveBase {

    @Override
    public UUID installCluster(HiveConfig config) {
        AbstractOperationHandler h = new InstallHandler(this, config);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID uninstallCluster(String clusterName) {
        AbstractOperationHandler h = new UninstallHandler(this, clusterName);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public List<HiveConfig> getClusters() {
        try {
            return pluginDao.getInfo(HiveConfig.PRODUCT_KEY, HiveConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get installation infos", ex);
        }
        return null;
    }

    @Override
    public HiveConfig getCluster(String clusterName) {
        try {
            return pluginDao.getInfo(HiveConfig.PRODUCT_KEY, clusterName, HiveConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get installation info {}", clusterName, ex);
        }
        return null;
    }

    @Override
    public UUID statusCheck(String clusterName, String hostname) {
        AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID startNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID stopNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID restartNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new RestartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID addNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID destroyNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public Map<Agent, Boolean> isInstalled(Set<Agent> nodes) {
        CheckInstallHandler h = new CheckInstallHandler(this);
        return h.check(Product.HIVE, nodes);
    }

    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, HiveConfig config, ProductOperation po) {
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            return new SetupStrategyOverHadoop(this, config, po);
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop(this, config, po);
            s.setEnvironment(env);
            return s;
        }
        return null;
    }

}
