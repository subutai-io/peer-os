package org.safehaus.subutai.plugin.hive.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Hive extends ApiBase<HiveConfig>
{

    public UUID installCluster( HiveConfig config, HadoopClusterConfig hc );

    public UUID uninstallCluster( String clusterName, HadoopClusterConfig hc );

    public UUID statusCheck( String clusterName, String hostname );

    public UUID startNode( String clusterName, String hostname );

    public UUID stopNode( String clusterName, String hostname );

    public UUID restartNode( String clusterName, String hostname );

    public UUID addNode( String clusterName, String hostname );

    public UUID uninstallNode( String clusterName, String hostname );

    public boolean isInstalled( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, HiveConfig config, TrackerOperation po );
}
