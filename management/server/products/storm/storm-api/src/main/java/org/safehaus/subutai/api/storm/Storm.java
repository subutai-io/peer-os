package org.safehaus.subutai.api.storm;

import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;

public interface Storm extends ApiBase<Config> {

	public UUID statusCheck(String clusterName, String hostname);

	public UUID startNode(String clusterName, String hostname);

	public UUID stopNode(String clusterName, String hostname);

	public UUID restartNode(String clusterName, String hostname);

	/**
	 * Adds a node to specified cluster.
	 *
	 * @param clusterName the name of cluster to which a node is added
	 * @param hostname    the name of the node to be added, <tt>null</tt> can be
	 *                    specified to in which case a new container is created
	 * @return operation id to track
	 */
	public UUID addNode(String clusterName, String hostname);

	public UUID destroyNode(String clusterName, String hostname);
}
