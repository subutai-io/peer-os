package org.safehaus.subutai.plugin.lucene.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Lucene extends ApiBase<Config>
{

    public UUID installCluster(Config config, HadoopClusterConfig hadoopConfig);

	public UUID addNode(String clusterName, String lxcHostname);

	public UUID destroyNode(String clusterName, String lxcHostname);

    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, Config config, ProductOperation po);


}
