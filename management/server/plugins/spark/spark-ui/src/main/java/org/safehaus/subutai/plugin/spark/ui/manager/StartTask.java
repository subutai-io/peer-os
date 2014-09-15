package org.safehaus.subutai.plugin.spark.ui.manager;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.ui.SparkUI;

import java.util.UUID;

public class StartTask implements Runnable {

    private final boolean master;
    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Spark spark;
    private final Tracker tracker;

    public StartTask(final Tracker tracker, final Spark spark,String clusterName, String lxcHostname, boolean master, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.master = master;
        this.spark = spark;
        this.tracker = tracker;
    }

    @Override
    public void run() {

        UUID trackID = spark.startNode(clusterName, lxcHostname, master);

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation(SparkClusterConfig.PRODUCT_KEY, trackID);
            if (po != null)
                if (po.getState() != ProductOperationState.RUNNING) {
                    if (po.getState() == ProductOperationState.SUCCEEDED)
                        state = NodeState.RUNNING;
                    break;
                }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
            if (System.currentTimeMillis() - start > 60 * 1000)
                break;
        }

        completeEvent.onComplete(state);
    }

}
