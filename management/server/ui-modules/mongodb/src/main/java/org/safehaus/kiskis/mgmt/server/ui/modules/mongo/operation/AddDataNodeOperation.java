/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
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

        addTask(Tasks.getKillRunningMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getUninstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getCleanMongoDataTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getAptGetUpdateTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getInstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getStopMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getRegisterIpsTask(clusterMembers));

        addTask(Tasks.getSetReplicaSetNameTask(
                config.getReplicaSetName(),
                Util.wrapAgentToSet(agent)));

        addTask(Tasks.getStartReplicaSetTask(
                Util.wrapAgentToSet(agent)));

        //find primary node task
        addTask(Tasks.getFindPrimaryNodeTask(config.getDataNodes().iterator().next()));

        //adjust uuid with real primary agent uuid during the operation
        addTask(Tasks.getRegisterSecondaryNodeWithPrimaryTask(agent,
                agent));

    }

}
