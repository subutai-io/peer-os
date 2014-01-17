/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class UninstallOperation extends Operation {

    public UninstallOperation(ClusterConfig config) {
        super("Uninstall Mongo cluster");

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        addTask(InstallTasks.getKillRunningMongoTask(clusterMembers));

        addTask(InstallTasks.getUninstallMongoTask(clusterMembers));

        addTask(InstallTasks.getCleanMongoDataTask(clusterMembers));
    }

}
