package org.safehaus.subutai.plugin.pig.api;


import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.UUID;


public interface Pig extends ApiBase<Config>
{

    public UUID installCluster( Config config, HadoopClusterConfig hadoopConfig );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, Config config, ProductOperation po);

}
