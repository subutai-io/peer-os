/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mongodb.manager;

import org.safehaus.subutai.shared.protocol.CompleteEvent;
import java.util.UUID;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.api.mongodb.Timeouts;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class CheckTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;

    public CheckTask(String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    public void run() {

        UUID trackID = MongoUI.getMongoManager().checkNode(clusterName, lxcHostname);

        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            ProductOperationView po = MongoUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
            if (po != null) {
                if (po.getState() != ProductOperationState.RUNNING) {
                    if (po.getLog().contains(NodeState.STOPPED.toString())) {
                        state = NodeState.STOPPED;
                    } else if (po.getLog().contains(NodeState.RUNNING.toString())) {
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
            if (System.currentTimeMillis() - start > (Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC + 3) * 1000) {
                break;
            }
        }

        completeEvent.onComplete(state);
    }

}
