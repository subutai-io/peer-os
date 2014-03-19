/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class AddRouterOperation extends Operation {

    public AddRouterOperation(Config config, Agent agent) {
        super("Add New Router");

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

        addTask(Tasks.getRegisterIpsTask(clusterMembers, config));

        addTask(Tasks.getStartRoutersTask(Util.wrapAgentToSet(agent), config.getConfigServers(), config));
    }

}
