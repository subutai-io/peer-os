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
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class DestroyNodeOperation extends Operation {

    public DestroyNodeOperation(Agent nodeAgent, Config config, NodeType nodeType) {
        super(null);

        if (nodeType == NodeType.CONFIG_NODE) {
            addTask(Tasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
            Task stopMongoTask = Tasks.getStopMongoTask(config.getRouterServers());
            stopMongoTask.setIgnoreExitCode(true);
            addTask(stopMongoTask);
            Set<Agent> otherConfigServers = new HashSet<Agent>(config.getConfigServers());
            otherConfigServers.remove(nodeAgent);
            Task startRoutersTask = Tasks.getStartRoutersTask(config.getRouterServers(), otherConfigServers, config);
            startRoutersTask.setIgnoreExitCode(true);
            addTask(startRoutersTask);
        } else if (nodeType == NodeType.DATA_NODE) {
            Task findPrimaryNodeTask = Tasks.getFindPrimaryNodeTask(nodeAgent, config);
            addTask(findPrimaryNodeTask);
            addTask(Tasks.getUnregisterSecondaryFromPrimaryTask(nodeAgent, nodeAgent, config));
            addTask(Tasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
        } else if (nodeType == NodeType.ROUTER_NODE) {
            addTask(Tasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(Tasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
        }
    }

}
