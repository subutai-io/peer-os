/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.hbase;

import org.safehaus.kiskis.mgmt.shared.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface HBase extends ApiBase<Config> {

    UUID startCluster(String clusterName);

    UUID stopCluster(String clusterName);

//    UUID checkNode(HBaseType type, String clusterName, String lxcHostname);

//    UUID startNodes(String clusterName);

//    UUID stopNodes(String clusterName);

    UUID checkCluster(String clusterName);
}
