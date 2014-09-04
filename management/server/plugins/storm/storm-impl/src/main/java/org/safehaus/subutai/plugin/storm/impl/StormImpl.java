package org.safehaus.subutai.plugin.storm.impl;

import org.safehaus.subutai.plugin.storm.impl.handler.UninstallHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StopHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.RestartHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StartHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StatusHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.DestroyNodeHandler;
import java.util.List;
import java.util.UUID;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.storm.api.StormConfig;

public class StormImpl extends StormBase {

    @Override
    public UUID installCluster(StormConfig config) {
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
    public List<StormConfig> getClusters() {
        return dbManager.getInfo(StormConfig.PRODUCT_NAME, StormConfig.class);
    }

    @Override
    public StormConfig getCluster(String clusterName) {
        return dbManager.getInfo(StormConfig.PRODUCT_NAME, clusterName, StormConfig.class);
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

}
