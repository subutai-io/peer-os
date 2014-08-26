/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.manager;

import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;
import org.safehaus.subutai.plugin.mongodb.ui.MongoUI;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * @author dilshat
 */
public class StartTask implements Runnable {

	private final NodeType nodeType;
	private final String clusterName, lxcHostname;
	private final CompleteEvent completeEvent;

	public StartTask(NodeType nodeType, String clusterName, String lxcHostname, CompleteEvent completeEvent) {
		this.clusterName = clusterName;
		this.lxcHostname = lxcHostname;
		this.completeEvent = completeEvent;
		this.nodeType = nodeType;
	}

	public void run() {

		UUID trackID = MongoUI.getMongoManager().startNode(clusterName, lxcHostname);

		long start = System.currentTimeMillis();
		NodeState state = NodeState.UNKNOWN;
		int waitTimeout = Timeouts.START_DATE_NODE_TIMEOUT_SEC;
		if (nodeType == NodeType.CONFIG_NODE) {
			waitTimeout = Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC;
		} else if (nodeType == NodeType.ROUTER_NODE) {
			waitTimeout = Timeouts.START_ROUTER_TIMEOUT_SEC;
		}

		while (!Thread.interrupted()) {
			ProductOperationView po = MongoUI.getTracker().getProductOperation(MongoClusterConfig.PRODUCT_KEY, trackID);
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
			if (System.currentTimeMillis() - start > (waitTimeout + 3) * 1000) {
				break;
			}
		}

		completeEvent.onComplete(state);
	}

}
