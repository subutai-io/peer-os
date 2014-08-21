/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.hadoop.manager.components;


import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * @author dilshat
 */
public class CheckTask implements Runnable {

	private final CompleteEvent completeEvent;
	private UUID trackID;
	private Config config;
	private Agent agent;
	private Boolean isDataNode;

	public CheckTask(Config config, CompleteEvent completeEvent, UUID trackID, Agent agent) {
		this.completeEvent = completeEvent;
		this.trackID = trackID;
		this.config = config;
		this.agent = agent;
	}

	public CheckTask(Config config, CompleteEvent completeEvent, UUID trackID, Agent agent, boolean isDataNode) {
		this.completeEvent = completeEvent;
		this.trackID = trackID;
		this.config = config;
		this.agent = agent;
		this.isDataNode = isDataNode;
	}

	public void run() {

		if (trackID != null) {
			while (true) {
				ProductOperationView prevPo = HadoopUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
				if (prevPo.getState() == ProductOperationState.RUNNING) {
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
		if (agent != null) {
			if (agent.equals(config.getNameNode())) {
				trackID = HadoopUI.getHadoopManager().statusNameNode(config);
			} else if (agent.equals(config.getJobTracker())) {
				trackID = HadoopUI.getHadoopManager().statusJobTracker(config);
			} else if (agent.equals(config.getSecondaryNameNode())) {
				trackID = HadoopUI.getHadoopManager().statusSecondaryNameNode(config);
			} else if (isDataNode != null) {
				if (isDataNode) {
					trackID = HadoopUI.getHadoopManager().statusDataNode(agent);
				} else {
					trackID = HadoopUI.getHadoopManager().statusTaskTracker(agent);
				}
			}


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
		}
		completeEvent.onComplete(state);
	}

}
