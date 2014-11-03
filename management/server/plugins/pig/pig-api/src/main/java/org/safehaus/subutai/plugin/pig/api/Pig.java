package org.safehaus.subutai.plugin.pig.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Pig extends ApiBase<PigConfig>
{

    public UUID installCluster( PigConfig config, HadoopClusterConfig hadoopConfig );

    public UUID destroyNode( String clusterName, ContainerHost containerHost );

    public UUID addNode( String clusterName, ContainerHost containerHost );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, PigConfig config, TrackerOperation po );
}
