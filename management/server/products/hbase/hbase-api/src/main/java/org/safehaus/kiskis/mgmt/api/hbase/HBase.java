/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.hbase;

import org.safehaus.kiskis.mgmt.shared.protocol.ApiBase;

import java.util.List;
import java.util.UUID;

//import org.safehaus.kiskis.mgmt.api.hadoop.Config;
//import org.safehaus.kiskis.mgmt.api.hbase.Config;

/**
 * @author dilshat
 */
public interface HBase extends ApiBase<org.safehaus.kiskis.mgmt.api.hbase.Config> {


    public UUID installCluster(org.safehaus.kiskis.mgmt.api.hbase.Config config);

    UUID startCluster(String clusterName);

    UUID stopCluster(String clusterName);

//    UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

//    UUID startNodes(String clusterName);

//    UUID stopNodes(String clusterName);

    UUID checkCluster(String clusterName);

    List<org.safehaus.kiskis.mgmt.api.hadoop.Config> getHadoopClusters();
    org.safehaus.kiskis.mgmt.api.hadoop.Config getHadoopCluster(String clusterName);

    public List<org.safehaus.kiskis.mgmt.api.hbase.Config> getClusters();
}
