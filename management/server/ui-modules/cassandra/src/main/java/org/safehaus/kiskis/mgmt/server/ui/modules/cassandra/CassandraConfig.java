/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author bahadyr
 */
public class CassandraConfig {

    private Set<Agent> selectedAgents = new HashSet<Agent>();

    public Set<Agent> getSelectedAgents() {
        return selectedAgents;
    }

    public void setSelectedAgents(Set<Agent> selectedAgents) {
        if (!Util.isCollectionEmpty(selectedAgents)) {
            this.selectedAgents = selectedAgents;
        }
    }

}
