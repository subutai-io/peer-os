/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.InstallTasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class AddDataNodeOperation extends Operation {

    public AddDataNodeOperation(ClusterConfig config, Agent agent) {
        super("Add New Data Node");

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        clusterMembers.add(agent);

        addTask(InstallTasks.getKillRunningMongoTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getUninstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getCleanMongoDataTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getAptGetUpdateTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getInstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getStopMongoTask(Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getRegisterIpsTask(clusterMembers));

        addTask(InstallTasks.getSetReplicaSetNameTask(
                config.getReplicaSetName(),
                Util.wrapAgentToSet(agent)));

        addTask(InstallTasks.getStartReplicaSetTask(
                Util.wrapAgentToSet(agent)));

        //find primary node task
        addTask(ManagerTasks.getFindPrimaryNodeTask(config.getDataNodes().iterator().next()));

        //adjust uuid with real primary agent uuid during the operation
        addTask(InstallTasks.getRegisterSecondaryNodeWithPrimaryTask(agent,
                agent));

    }

}
