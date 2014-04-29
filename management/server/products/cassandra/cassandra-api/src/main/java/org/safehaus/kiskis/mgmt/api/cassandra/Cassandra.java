/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.cassandra;

import java.util.List;
import java.util.UUID;

/**
 * @author dilshat
 */
public interface Cassandra {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();

    UUID startAllNodes(String clusterName);

    UUID checkAllNodes(String clusterName);

    UUID stopAllNodes(String clusterName);
}
