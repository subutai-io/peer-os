package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Deletion {

    private HadoopImpl parent;

    public Deletion(HadoopImpl parent) {
        this.parent = parent;
    }

    public UUID execute(final String clusterName) {
        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                Config config = parent.getDbManager().getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                po.addLog("Updating db...");
                if (parent.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

                po.addLog("Destroying lxc containers...");

                try {
                    parent.getLxcManager().destroyLxcs(new HashSet<Agent>(config.getAllNodes()));
                    po.addLog("Lxc containers successfully destroyed");
                } catch (LxcDestroyException ex) {
                    po.addLog(String.format("%s, skipping...", ex.getMessage()));
                }
            }
        });

        return po.getId();
    }
}
