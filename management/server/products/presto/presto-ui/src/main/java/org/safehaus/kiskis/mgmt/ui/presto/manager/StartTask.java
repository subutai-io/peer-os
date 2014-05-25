/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.presto.manager;

import org.safehaus.kiskis.mgmt.api.presto.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.presto.PrestoUI;

import java.util.UUID;

/**
 * @author dilshat
 */
public class StartTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;

    public StartTask(String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    public void run() {

        UUID trackID = PrestoUI.getPrestoManager().startNode(clusterName, lxcHostname);

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while (!Thread.interrupted()) {
            ProductOperationView po = PrestoUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
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
            if (System.currentTimeMillis() - start > 60 * 1000) {
                break;
            }
        }

        completeEvent.onComplete(state);
    }

}
