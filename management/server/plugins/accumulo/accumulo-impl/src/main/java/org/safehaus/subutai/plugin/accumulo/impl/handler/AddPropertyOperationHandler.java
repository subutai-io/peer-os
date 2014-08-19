package org.safehaus.subutai.plugin.accumulo.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
	private final ProductOperation po;
	private final String propertyName;
	private final String propertyValue;


	public AddPropertyOperationHandler(AccumuloImpl manager, String clusterName, String propertyName,
	                                   String propertyValue) {
		super(manager, clusterName);
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		po = manager.getTracker().createProductOperation(AccumuloClusterConfig.PRODUCT_KEY,
				String.format("Adding property %s=%s", propertyName, propertyValue));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(propertyName)) {
			po.addLogFailed("Malformed arguments\nOperation aborted");
			return;
		}
		final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster(clusterName);
		if (accumuloClusterConfig == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		po.addLog("Adding property...");

		Command addPropertyCommand =
				Commands.getAddPropertyCommand(propertyName, propertyValue, accumuloClusterConfig.getAllNodes());
		manager.getCommandRunner().runCommand(addPropertyCommand);

		if (addPropertyCommand.hasSucceeded()) {
			po.addLog("Property added successfully\nRestarting cluster...");

			Command restartClusterCommand = Commands.getRestartCommand(accumuloClusterConfig.getMasterNode());
			manager.getCommandRunner().runCommand(restartClusterCommand);
			if (restartClusterCommand.hasSucceeded()) {
				po.addLogDone("Cluster restarted successfully");
			} else {
				po.addLogFailed(String.format("Cluster restart failed, %s", restartClusterCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed(String.format("Adding property failed, %s", addPropertyCommand.getAllErrors()));
		}
	}
}
