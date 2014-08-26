package org.safehaus.subutai.ui.hadoop.manager.components;

import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.hadoop.HadoopUI;

import java.util.UUID;

/**
 * Created by daralbaev on 17.04.14.
 */
public class WaitTask implements Runnable {
	private final CompleteEvent completeEvent;
	private UUID trackID;

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
