package org.safehaus.subutai.plugin.shark.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Shark extends ApiBase<SharkClusterConfig>
{

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID actualizeMasterIP( String clusterName );

    public ClusterSetupStrategy getClusterSetupStrategy( TrackerOperation po, SharkClusterConfig config,
                                                         Environment environment );
}

