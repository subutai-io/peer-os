package org.safehaus.subutai.plugin.pig.api;


import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;


public interface Pig extends ApiBase<Config> {


	public UUID destroyNode(String clusterName, String lxcHostname);
}
