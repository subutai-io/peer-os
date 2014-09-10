package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Handles ZK config property addition
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
	private final ProductOperation po;
	private final String fileName;
	private final String propertyName;
	private final String propertyValue;


	public AddPropertyOperationHandler(ZookeeperImpl manager, String clusterName, String fileName, String propertyName,
	                                   String propertyValue) {
		super(manager, clusterName);
		this.fileName = fileName;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		po = manager.getTracker().createProductOperation(ZookeeperClusterConfig.PRODUCT_KEY,
				String.format("Adding property %s=%s to file %s", propertyName, propertyValue, fileName));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(fileName) || Strings
				.isNullOrEmpty(propertyName)) {
			po.addLogFailed("Malformed arguments\nOperation aborted");
			return;
		}
		final ZookeeperClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		po.addLog("Adding property...");

		Command addPropertyCommand =
				Commands.getAddPropertyCommand(fileName, propertyName, propertyValue, config.getNodes());
		manager.getCommandRunner().runCommand(addPropertyCommand);

		if (addPropertyCommand.hasSucceeded()) {
			po.addLog("Property added successfully\nRestarting cluster...");

			Command restartCommand = Commands.getRestartCommand(config.getNodes());
			final AtomicInteger count = new AtomicInteger();
			manager.getCommandRunner().runCommand(restartCommand, new CommandCallback() {
				@Override
				public void onResponse(Response response, AgentResult agentResult, Command command) {
					if (agentResult.getStdOut().contains("STARTED")) {
						if (count.incrementAndGet() == config.getNodes().size()) {
							stop();
						}
					}
				}
			});

			if (count.get() == config.getNodes().size()) {
				po.addLogDone("Cluster successfully restarted");
			} else {
				po.addLogFailed(String.format("Failed to restart cluster, %s", restartCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed(String.format("Adding property failed, %s", addPropertyCommand.getAllErrors()));
		}
	}
}
