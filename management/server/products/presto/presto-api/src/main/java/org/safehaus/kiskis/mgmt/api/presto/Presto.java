/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.presto;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Presto {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID addWorkerNode(String clusterName, String lxcHostname);

    public UUID destroyWorkerNode(String clusterName, String lxcHostname);

    public UUID changeCoordinatorNode(String clusterName, String newMasterHostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();

    /**
     * Starts the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @param master - specifies if this commands affects master or slave
     * running on this node true - master, false - slave
     * @return - UUID of operation to track
     *
     */
    public UUID startNode(String clusterName, String lxcHostName, boolean master);

    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @param master - specifies if this commands affects master or slave
     * running on this node true - master, false - slave
     * @return - UUID of operation to track
     *
     */
    public UUID stopNode(String clusterName, String lxcHostName, boolean master);

    /**
     * Checks status of the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     * @return - UUID of operation to track
     *
     */
    public UUID checkNode(String clusterName, String lxcHostName);
}
