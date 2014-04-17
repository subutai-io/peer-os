package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 17.04.14.
 */
public class WaitTask implements Runnable {
    private UUID trackID;
    private final CompleteEvent completeEvent;

    public WaitTask(UUID trackID, CompleteEvent completeEvent) {
        this.trackID = trackID;
        this.completeEvent = completeEvent;
    }

    @Override
    public void run() {
        if (trackID != null) {
            while (true) {
                ProductOperationView po = HadoopUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
                if (po.getState() == ProductOperationState.RUNNING) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        NodeState state = NodeState.UNKNOWN;
        completeEvent.onComplete(state);
    }
}
