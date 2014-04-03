/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.spark;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Spark {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID addSlaveNode(String clusterName, String lxcHostname);

    public UUID destroySlaveNode(String clusterName, String lxcHostname);

    public UUID changeMasterNode(String clusterName, String newMasterHostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();
}
