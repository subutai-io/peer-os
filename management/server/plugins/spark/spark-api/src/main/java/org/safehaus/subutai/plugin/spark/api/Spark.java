package org.safehaus.subutai.plugin.spark.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public interface Spark extends ApiBase<SparkClusterConfig>
{

    public UUID installCluster( SparkClusterConfig config, HadoopClusterConfig hadoopConfig );

    public UUID addSlaveNode( String clusterName, String lxcHostname );

    public UUID destroySlaveNode( String clusterName, String lxcHostname );

    public UUID changeMasterNode( String clusterName, String newMasterHostname, boolean keepSlave );

    /**
     * Starts the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @param master - specifies if this commands affects master or slave running on this node true - master, false -
     * slave
     *
     * @return - UUID of operation to track
     */
    public UUID startNode( String clusterName, String lxcHostName, boolean master );


    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - master node name
     *
     * @return - UUID of operation to track
     */
    public UUID startCluster( String clusterName, String lxcHostName );

    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @param master - specifies if this commands affects master or slave running on this node true - master, false -
     * slave
     *
     * @return - UUID of operation to track
     */
    public UUID stopNode( String clusterName, String lxcHostName, boolean master );


    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - master node name
     *
     * @return - UUID of operation to track
     */
    public UUID stopCluster( String clusterName, String lxcHostName );

    /**
     * Checks status of the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     *
     * @return - UUID of operation to track
     */
    public UUID checkNode( String clusterName, String lxcHostName, boolean master );


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SparkClusterConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( TrackerOperation po, SparkClusterConfig clusterConfig,
                                                         Environment environment );
}
