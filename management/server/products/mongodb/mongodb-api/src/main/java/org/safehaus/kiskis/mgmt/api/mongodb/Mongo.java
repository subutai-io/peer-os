/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.mongodb;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;

/**
 *
 * @author dilshat
 */
public interface Mongo {

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();

    /**
     * Installs cluster according to the specified configuration
     *
     * @param config - cluster configuration
     * @return - UUID of operation to track
     *
     */
    public UUID installCluster(Config config);

    /**
     * Uninstalls the specified cluster
     *
     * @param clusterName - name of cluster
     * @return - UUID of operation to track
     *
     */
    public UUID uninstallCluster(String clusterName);

    /**
     * adds node to the specified cluster
     *
     * @param clusterName - name of cluster
     * @param nodeType - type of node to add
     * @return - UUID of operation to track
     *
     */
    public UUID addNode(String clusterName, NodeType nodeType);

    /**
     * destroys node in the specified cluster
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @return - UUID of operation to track
     *
     */
    public UUID destroyNode(String clusterName, String lxcHostName);

    /**
     * Starts the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @return - UUID of operation to track
     *
     */
    public UUID startNode(String clusterName, String lxcHostName);

    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @return - UUID of operation to track
     *
     */
    public UUID stopNode(String clusterName, String lxcHostName);

    /**
     * Checks status of the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @return - UUID of operation to track
     *
     */
    public UUID checkNode(String clusterName, String lxcHostName);

    /**
     * Returns view of product operation
     *
     * @param viewId - operation view id
     * @return - product operation view
     *
     */
    public ProductOperationView getProductOperationView(UUID viewId);
}
