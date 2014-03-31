/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.solr;

import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Solr {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID startNode(String clusterName, String lxcHostname);

    public UUID stopNode(String clusterName, String lxcHostname);

    public UUID checkNode(String clusterName, String lxcHostname);

    public UUID addNode(String clusterName);

    public UUID destroyNode(String clusterName, String lxcHostname);
}
