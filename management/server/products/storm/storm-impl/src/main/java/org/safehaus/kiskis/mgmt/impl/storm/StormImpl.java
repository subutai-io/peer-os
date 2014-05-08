package org.safehaus.kiskis.mgmt.impl.storm;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.impl.storm.handler.*;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;

public class StormImpl extends StormBase {

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UUID destroyNode(String clusterName, String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_NAME, Config.class);
    }

    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_NAME, clusterName, Config.class);
    }

}
