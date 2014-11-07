package org.safehaus.subutai.plugin.storm.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Storm extends ApiBase<StormClusterConfiguration>
{

    public UUID startNode( String clusterName, String hostname );

    public UUID stopNode( String clusterName, String hostname );

    public UUID checkNode( String clusterName, String hostname );

    public UUID restartNode( String clusterName, String hostname );

    /**
     * Adds a node to specified cluster.
     *
     * @param clusterName the name of cluster to which a node is added
     *
     * @return operation id to track
     */
    public UUID addNode( String clusterName );

    public UUID destroyNode( String clusterName, String hostname );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( StormClusterConfiguration config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, StormClusterConfiguration config,
                                                         TrackerOperation po );
}
