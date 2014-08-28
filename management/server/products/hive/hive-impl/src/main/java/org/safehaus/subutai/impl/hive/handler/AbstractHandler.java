package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Iterator;

abstract class AbstractHandler extends AbstractOperationHandler<HiveImpl> {

	public AbstractHandler(HiveImpl manager, String clusterName) {
		super(manager, clusterName);
	}

	boolean isServerNode(Config config, String hostname) {
		return config.getServer().getHostname().equalsIgnoreCase(hostname);
	}

	/**
	 * Checks if client nodes are connected and, optionally, removes nodes that
	 * are not connected.
	 *
	 * @param removeDisconnected
	 * @return number of connected nodes
	 */
	int checkClientNodes(Config config, boolean removeDisconnected) {
		int connected = 0;
		Iterator<Agent> it = config.getClients().iterator();
		while (it.hasNext()) {
			if (isNodeConnected(it.next().getHostname())) connected++;
			else if (removeDisconnected) it.remove();
		}
		return connected;
	}

	boolean isNodeConnected(String hostname) {
		return manager.getAgentManager().getAgentByHostname(hostname) != null;
	}

	boolean isZero(Integer i) {
		return i != null && i == 0;
	}
}
