package org.safehaus.subutai.api.hive;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Hive extends ApiBase<Config> {

	public UUID statusCheck(String clusterName, String hostname);

	public UUID startNode(String clusterName, String hostname);

	public UUID stopNode(String clusterName, String hostname);

	public UUID restartNode(String clusterName, String hostname);

	public UUID addNode(String clusterName, String hostname);

	public UUID destroyNode(String clusterName, String hostname);

	public Map<Agent, Boolean> isInstalled(Set<Agent> nodes);
}
