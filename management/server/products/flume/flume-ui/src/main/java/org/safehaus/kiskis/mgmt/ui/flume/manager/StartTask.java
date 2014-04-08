package org.safehaus.kiskis.mgmt.ui.flume.manager;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.flume.FlumeUI;

public class StartTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;

    public StartTask(String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    public void run() {

        UUID trackID = FlumeUI.getFlumeManager().startNode(clusterName, lxcHostname);

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while (!Thread.interrupted()) {
            ProductOperationView po = FlumeUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
            if (po != null) {
                if (po.getState() != ProductOperationState.RUNNING) {
                    if (po.getState() == ProductOperationState.SUCCEEDED) {
                        state = NodeState.RUNNING;
                    }
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
            if (System.currentTimeMillis() - start > (30 + 3) * 1000) {
                break;
            }
        }

        completeEvent.onComplete(state);
    }

}
