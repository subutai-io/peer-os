package org.safehaus.subutai.plugin.spark.ui.manager;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.ui.SparkUI;

import java.util.UUID;

public class StopTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final boolean master;
    private final CompleteEvent completeEvent;

    public StopTask(String clusterName, String lxcHostname, boolean master, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.master = master;
    }

    @Override
    public void run() {

        UUID trackID = SparkUI.getSparkManager().stopNode(clusterName, lxcHostname, master);

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while (!Thread.interrupted()) {
            ProductOperationView po = SparkUI.getTracker().getProductOperation(SparkClusterConfig.PRODUCT_KEY, trackID);
            if (po != null)
                if (po.getState() != ProductOperationState.RUNNING) {
                    if (po.getState() == ProductOperationState.SUCCEEDED)
                        state = NodeState.STOPPED;
                    break;
                }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
            if (System.currentTimeMillis() - start > 30 * 1000)
                break;
        }

        completeEvent.onComplete(state);
    }

}
