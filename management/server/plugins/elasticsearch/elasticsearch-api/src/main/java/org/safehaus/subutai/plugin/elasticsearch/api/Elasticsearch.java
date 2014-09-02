package org.safehaus.subutai.plugin.elasticsearch.api;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.UUID;


public interface Elasticsearch extends ApiBase<Config> {

	UUID startAllNodes(String clusterName);

	UUID checkAllNodes(String clusterName);

	UUID stopAllNodes(String clusterName);

    ClusterSetupStrategy getClusterSetupStrategy( Environment environment, Config config, ProductOperation po );

}
