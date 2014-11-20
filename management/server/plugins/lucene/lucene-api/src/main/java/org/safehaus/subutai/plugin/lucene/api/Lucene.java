package org.safehaus.subutai.plugin.lucene.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Lucene extends ApiBase<LuceneConfig>
{

    public UUID installCluster( LuceneConfig config, HadoopClusterConfig hadoopConfig );

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID uninstallNode( String clusterName, String lxcHostname );

    public UUID uninstallCluster( LuceneConfig config );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( LuceneConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, LuceneConfig config, TrackerOperation po );
}
