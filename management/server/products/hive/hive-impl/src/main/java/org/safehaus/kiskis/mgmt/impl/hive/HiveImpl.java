package org.safehaus.kiskis.mgmt.impl.hive;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.handler.*;

public class HiveImpl extends HiveBase {

    public UUID installCluster(Config config) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Installing cluster " + config.getClusterName());
        executor.execute(new InstallHandler(this, config.getClusterName(), po));
        return po.getId();
    }

    public UUID uninstallCluster(String clusterName) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Uninstalling cluster " + clusterName);
        executor.execute(new UninstallHandler(this, clusterName, po));
        return po.getId();
    }

    public UUID statusCheck(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Status check for " + hostname);
        BaseHandler h = new StatusHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID startNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Start node " + hostname);
        BaseHandler h = new StartHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID stopNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Stop node " + hostname);
        BaseHandler h = new StopHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID restartNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Restart node " + hostname);
        BaseHandler h = new RestartHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID addNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Add node to cluster: " + hostname);
        BaseHandler h = new AddNodeHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID destroyNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Remove node from cluster: " + hostname);
        BaseHandler h = new DestroyNodeHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

}
