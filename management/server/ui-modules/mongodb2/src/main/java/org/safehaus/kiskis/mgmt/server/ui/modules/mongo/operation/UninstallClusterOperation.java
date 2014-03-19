/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class UninstallClusterOperation extends Operation {

    public UninstallClusterOperation(Config config) {
        super("Uninstall Mongo cluster");

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        addTask(Tasks.getKillRunningMongoTask(clusterMembers));

        addTask(Tasks.getUninstallMongoTask(clusterMembers));

        addTask(Tasks.getCleanMongoDataTask(clusterMembers));
    }

}
