/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.hadoop.operation.configuration;


import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.common.protocol.CompleteEvent;

import java.util.UUID;

/**
 * @author dilshat
 */
public class CheckTask implements Runnable {

	private final CompleteEvent completeEvent;
	private UUID trackID;

	public CheckTask(UUID trackID, CompleteEvent completeEvent) {
		this.completeEvent = completeEvent;
		this.trackID = trackID;
	}

	public void run() {

		if (trackID != null) {
			while (true) {
				ProductOperationView prevPo = HadoopImpl.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
				if (prevPo == null || prevPo.getState() == ProductOperationState.RUNNING) {
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

		long start = System.currentTimeMillis();
		while (!Thread.interrupted()) {
			ProductOperationView po = HadoopImpl.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
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
