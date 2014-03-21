/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.api.mongodb;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

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
     * Installs cluster according to specified configuration
     *
     * @param config - cluster configuration
     * @return - UUID of operation to track
     *
     */
    public UUID installCluster(Config config);

    /**
     * Uninstalls the specified cluster
     *
     * @param config - cluster configuration
     * @return - UUID of operation to track
     *
     */
    public UUID uninstallCluster(Config config);

    /**
     * adds node to the specified cluster
     *
     * @param config - cluster configuration
     * @param nodeType - type of node to add
     * @return - UUID of operation to track
     *
     */
    public UUID addNode(Config config, NodeType nodeType);

    /**
     * destroys node in the specified cluster
     *
     * @param config - cluster configuration
     * @param agent - agent of node
     * @return - UUID of operation to track
     *
     */
    public UUID destroyNode(Config config, Agent agent);

    /**
     * Starts the specified node
     *
     * @param config - cluster configuration
     * @param agent - agent of node
     * @return - result of operation true - success, false - failure
     *
     */
    public boolean startNode(Config config, Agent node);

    /**
     * Stops the specified node
     *
     * @param config - cluster configuration
     * @param agent - agent of node
     * @return - result of operation true - success, false - failure
     *
     */
    public boolean stopNode(Config config, Agent node);

    /**
     * Checks status of the specified node
     *
     * @param config - cluster configuration
     * @param agent - agent of node
     * @return - node state
     *
     */
    public NodeState checkNode(Config config, Agent node);
}
