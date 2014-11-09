package org.safehaus.subutai.plugin.pig.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Pig extends ApiBase<PigConfig>
{

    public UUID installCluster( PigConfig config, HadoopClusterConfig hadoopConfig );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID uninstallCluster( PigConfig config );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( PigConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, PigConfig config, TrackerOperation po );

}
