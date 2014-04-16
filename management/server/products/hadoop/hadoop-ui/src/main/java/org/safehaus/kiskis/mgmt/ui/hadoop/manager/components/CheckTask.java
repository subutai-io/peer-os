/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;


import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * @author dilshat
 */
public class CheckTask implements Runnable {

    private final CompleteEvent completeEvent;
    private final UUID prevTaskID;
    private UUID trackID;
    private Config config;

    public CheckTask(CompleteEvent completeEvent, UUID previousTaskId, UUID trackID) {
        this.completeEvent = completeEvent;
        this.prevTaskID = previousTaskId;
        this.trackID = trackID;
    }

    public CheckTask(Config config, CompleteEvent completeEvent, UUID previousTaskId, UUID trackID) {
        this.completeEvent = completeEvent;
        this.prevTaskID = previousTaskId;
        this.trackID = trackID;
        this.config = config;
    }

    public void run() {

        if (prevTaskID != null) {

            while (true) {
                ProductOperationView prevPo = HadoopUI.getTracker().getProductOperation(Config.PRODUCT_KEY, prevTaskID);
                if (prevPo.getState() == ProductOperationState.RUNNING) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    break;
                }
            }
        }

        if (trackID == null && config != null) {
            trackID = HadoopUI.getHadoopManager().statusNameNode(config);
        }

        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            ProductOperationView po = HadoopUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
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
            if (System.currentTimeMillis() - start > (30 + 3) * 1000) {
                break;
            }
        }

        completeEvent.onComplete(state);
    }

}
