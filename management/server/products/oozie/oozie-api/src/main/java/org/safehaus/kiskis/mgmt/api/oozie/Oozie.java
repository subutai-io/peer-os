/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.oozie;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Oozie {

    public UUID installCluster(OozieConfig config);

    public UUID uninstallCluster(String clusterName);

    public UUID destroyNode(String clusterName, String lxcHostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<OozieConfig> getClusters();

    UUID startServer(Agent agent);
    UUID stopServer(Agent agent);

    UUID checkServerStatus(Agent agent);
}
