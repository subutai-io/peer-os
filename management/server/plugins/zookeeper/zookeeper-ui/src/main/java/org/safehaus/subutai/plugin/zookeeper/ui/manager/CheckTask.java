/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.manager;

import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.ui.ZookeeperUI;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.enums.NodeState;

import java.util.UUID;

/**
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

		UUID trackID = ZookeeperUI.getManager().checkNode(clusterName, lxcHostname);

		NodeState state = NodeState.UNKNOWN;
		long start = System.currentTimeMillis();
		while (!Thread.interrupted()) {
			ProductOperationView po = ZookeeperUI.getTracker().getProductOperation(ZookeeperClusterConfig.PRODUCT_KEY, trackID);
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
