package org.safehaus.subutai.plugin.nutch.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Nutch extends ApiBase<NutchConfig>
{

    public UUID installCluster( NutchConfig config, HadoopClusterConfig hadoopConfig );

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID uninstallCluster( NutchConfig config );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( NutchConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, NutchConfig config, TrackerOperation po );
}
