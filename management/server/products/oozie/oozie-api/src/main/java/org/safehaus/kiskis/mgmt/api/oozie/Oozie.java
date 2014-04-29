/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.oozie;

import java.util.List;
import java.util.UUID;

/**
 * @author dilshat
 */
public interface Oozie {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(Config config);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();

    UUID startServer(Config config);

    UUID stopServer(Config config);

    UUID checkServerStatus(Config config);
}
