/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.spark.manager;

import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.ui.spark.SparkUI;

import java.util.UUID;

/**
 * @author dilshat
 */
public class CheckTask implements Runnable {

	private final String clusterName, lxcHostname;
	private final boolean master;
	private final CompleteEvent completeEvent;

	public CheckTask(String clusterName, String lxcHostname, boolean master, CompleteEvent completeEvent) {
		this.clusterName = clusterName;
		this.lxcHostname = lxcHostname;
		this.completeEvent = completeEvent;
		this.master = master;
	}

	public void run() {

		UUID trackID = SparkUI.getSparkManager().checkNode(clusterName, lxcHostname);

		NodeState state = NodeState.UNKNOWN;
		long start = System.currentTimeMillis();
		while (!Thread.interrupted()) {
			ProductOperationView po = SparkUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
			if (po != null) {
				if (po.getState() != ProductOperationState.RUNNING) {
					if (master) {
						if (po.getLog().contains("Spark Master is running")) {
							state = NodeState.RUNNING;
						} else if (po.getLog().contains("Spark Master is NOT running")) {
							state = NodeState.STOPPED;
						}
					} else {
						if (po.getLog().contains("Spark Worker is running")) {
							state = NodeState.RUNNING;
						} else if (po.getLog().contains("Spark Worker is NOT running")) {
							state = NodeState.STOPPED;
						}
					}
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				break;
			}
			if (System.currentTimeMillis() - start > 30 * 1000) {
				break;
			}
		}

		completeEvent.onComplete(state);
	}

}
