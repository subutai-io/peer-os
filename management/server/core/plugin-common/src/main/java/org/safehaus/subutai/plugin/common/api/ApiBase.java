package org.safehaus.subutai.plugin.common.api;


import java.util.List;
import java.util.UUID;


/**
 * Created by dilshat on 5/1/14.
 */
public interface ApiBase<T extends ConfigBase>
{

    /**
     * Installs cluster according to the specified configuration object
     *
     * @param config - cluster configuration object
     *
     * @return - UUID of operation to track
     */
    public UUID installCluster( T config );

    /**
     * Uninstalls the specified cluster
     *
     * @param clusterName - name of cluster
     *
     * @return - UUID of operation to track
     */
    public UUID uninstallCluster( String clusterName );

    /**
     * Returns list of configuration objects of installed clusters
     *
     * @return - list of configuration objects of installed clusters
     */
    public List<T> getClusters();

    /**
     * Returns configuration object of installed cluster by name
     *
     * @param clusterName - - name of cluster
     *
     * @return - configuration object of installed cluster
     */
    public T getCluster( String clusterName );

    /**
     * Add specified node to specified cluster
     *
     * @param clusterName - name of cluster
     * @param agentHostName - name of node to be added to cluster
     *
     * @return - UUID of operation to track
     */
    public UUID addNode( String clusterName, String agentHostName );
}
