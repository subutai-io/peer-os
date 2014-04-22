/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.oozie.manager;

import org.safehaus.kiskis.mgmt.api.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.oozie.OozieUI;

import java.util.UUID;

/**
 * @author dilshat
 */
public class StartTask implements Runnable {

//    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Agent server;

    public StartTask(Agent server, CompleteEvent completeEvent) {
//        this.clusterName = clusterName;
//        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.server = server;
    }

    public void run() {

        UUID trackID = OozieUI.getOozieManager().startServer(server);

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while (!Thread.interrupted()) {
            ProductOperationView po = OozieUI.getTracker().getProductOperation(OozieConfig.PRODUCT_KEY, trackID);
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
