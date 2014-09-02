package org.safehaus.subutai.plugin.lucene.api;


import java.util.UUID;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.api.manager.helper.Environment;


public interface Lucene extends ApiBase<Config> {

    public UUID installCluster(Config config, HadoopClusterConfig hadoopConfig);

	public UUID addNode(String clusterName, String lxcHostname);

	public UUID destroyNode(String clusterName, String lxcHostname);

    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, Config config, ProductOperation po);


}
