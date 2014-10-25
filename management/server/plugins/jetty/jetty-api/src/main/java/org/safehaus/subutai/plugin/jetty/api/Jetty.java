package org.safehaus.subutai.plugin.jetty.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Jetty extends ApiBase<JettyConfig>
{
    UUID startCluster( String clusterName );

    UUID checkCluster( String clusterName );

    UUID stopCluster( String clusterName );

    UUID startService( String clusterName, String lxchostname );

    UUID stopService( String clusterName, String lxchostname );

    UUID statusService( String clusterName, String lxchostname );

    EnvironmentBuildTask getDefaultEnvironmentBlueprint( JettyConfig config ) throws ClusterSetupException;

    ClusterSetupStrategy getClusterSetupStrategy( Environment env, JettyConfig config, TrackerOperation po );
}