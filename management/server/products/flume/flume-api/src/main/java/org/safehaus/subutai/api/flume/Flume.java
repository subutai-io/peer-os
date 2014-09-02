package org.safehaus.subutai.api.flume;

import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;

public interface Flume extends ApiBase<Config> {


	public UUID startNode(String clusterName, String lxcHostname);

	public UUID stopNode(String clusterName, String lxcHostname);

	public UUID checkNode(String clusterName, String lxcHostname);

	public UUID addNode(String clusterName, String lxcHostname);

	public UUID destroyNode(String clusterName, String lxcHostname);

}
