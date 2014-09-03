package org.safehaus.subutai.impl.zookeeper.handler;

import com.google.common.base.Strings;
import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandCallback;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dilshat on 5/7/14.
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
	private final ProductOperation po;
	private final String fileName;
	private final String propertyName;
	private final String propertyValue;

	public AddPropertyOperationHandler(ZookeeperImpl manager, String clusterName, String fileName, String propertyName, String propertyValue) {
		super(manager, clusterName);
		this.fileName = fileName;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Adding property %s=%s to file %s", propertyName, propertyValue, fileName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(fileName) || Strings.isNullOrEmpty(propertyName)) {
			po.addLogFailed("Malformed arguments\nOperation aborted");
			return;
		}
		final Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		po.addLog("Adding property...");

		Command addPropertyCommand = Commands.getAddPropertyCommand(fileName, propertyName, propertyValue, config.getNodes());
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
