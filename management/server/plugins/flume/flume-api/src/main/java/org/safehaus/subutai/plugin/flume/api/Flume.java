package org.safehaus.subutai.plugin.flume.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Flume extends ApiBase<FlumeConfig>
{

    public UUID installCluster( FlumeConfig config, HadoopClusterConfig hadoopConfig );

    public UUID startNode( String clusterName, String lxcHostname );

    public UUID stopNode( String clusterName, String lxcHostname );

    public UUID checkNode( String clusterName, String lxcHostname );

    public UUID checkServiceStatus( String clusterName, String lxcHostname );

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID uninstallCluster( FlumeConfig config );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( FlumeConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, FlumeConfig config, TrackerOperation po );
}
