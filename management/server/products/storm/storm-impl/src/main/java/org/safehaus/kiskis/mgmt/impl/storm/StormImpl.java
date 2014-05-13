package org.safehaus.kiskis.mgmt.impl.storm;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.impl.storm.handler.*;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;

public class StormImpl extends StormBase {

    @Override
    public UUID installCluster(Config config) {
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
    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_NAME, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_NAME, clusterName, Config.class);
    }

}
