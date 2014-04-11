/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.hbase;

import java.util.List;
import java.util.UUID;

/**
 * @author dilshat
 */
public interface HBase {

    public UUID installCluster(HBaseConfig config);

//    public UUID uninstallCluster(String clusterName);

    public UUID uninstallCluster(HBaseConfig config);

    public UUID startNode(String clusterName, String lxcHostname);

    public UUID stopNode(String clusterName, String lxcHostname);

    public UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

    public UUID addNode(String clusterName);

    public UUID destroyNode(String clusterName, String lxcHostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<HBaseConfig> getClusters();
}
