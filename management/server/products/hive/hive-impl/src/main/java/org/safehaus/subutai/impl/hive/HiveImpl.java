package org.safehaus.subutai.impl.hive;

import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.hive.handler.*;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HiveImpl extends HiveBase {

	@Override
	public UUID installCluster(Config config) {
		AbstractOperationHandler h = new InstallHandler(this, config);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID uninstallCluster(String clusterName) {
		AbstractOperationHandler h = new UninstallHandler(this, clusterName);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	@Override
	public UUID statusCheck(String clusterName, String hostname) {
		AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID startNode(String clusterName, String hostname) {
		AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID stopNode(String clusterName, String hostname) {
		AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID restartNode(String clusterName, String hostname) {
		AbstractOperationHandler h = new RestartHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID addNode(String clusterName, String hostname) {
		AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID destroyNode(String clusterName, String hostname) {
		AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public Map<Agent, Boolean> isInstalled(Set<Agent> nodes) {
		CheckInstallHandler h = new CheckInstallHandler(this);
		return h.check(Product.HIVE, nodes);
	}

}
