package org.safehaus.subutai.plugin.hive.api;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Hive extends ApiBase<HiveConfig> {

	public UUID statusCheck(String clusterName, String hostname);

	public UUID startNode(String clusterName, String hostname);

	public UUID stopNode(String clusterName, String hostname);

	public UUID restartNode(String clusterName, String hostname);

	public UUID addNode(String clusterName, String hostname);

	public UUID destroyNode(String clusterName, String hostname);

	public Map<Agent, Boolean> isInstalled(Set<Agent> nodes);
}
