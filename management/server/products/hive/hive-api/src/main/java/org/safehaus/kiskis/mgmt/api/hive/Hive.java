package org.safehaus.kiskis.mgmt.api.hive;

import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

public interface Hive extends ApiBase<Config> {

    public UUID statusCheck(String clusterName, String hostname);

    public UUID startNode(String clusterName, String hostname);

    public UUID stopNode(String clusterName, String hostname);

    public UUID restartNode(String clusterName, String hostname);

    public UUID addNode(String clusterName, String hostname);

    public UUID destroyNode(String clusterName, String hostname);
}
