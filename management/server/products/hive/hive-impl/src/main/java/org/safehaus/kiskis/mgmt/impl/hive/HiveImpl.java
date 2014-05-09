package org.safehaus.kiskis.mgmt.impl.hive;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.handler.*;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;

public class HiveImpl extends HiveBase {

    public UUID installCluster(Config config) {
        AbstractOperationHandler h = new InstallHandler(this, config);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID uninstallCluster(String clusterName) {
        AbstractOperationHandler h = new UninstallHandler(this, clusterName);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID statusCheck(String clusterName, String hostname) {
        AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID startNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID stopNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID restartNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new RestartHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID addNode(String clusterName, String hostname) {
        AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
        executor.execute(h);
        return h.getTrackerId();
    }

    public UUID destroyNode(String clusterName, String hostname) {
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
