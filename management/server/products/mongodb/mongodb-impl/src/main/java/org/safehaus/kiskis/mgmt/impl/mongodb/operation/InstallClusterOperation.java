/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb.operation;

import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Tasks;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class InstallClusterOperation extends Operation {

    private final Config config;

    public Config getConfig() {
        return config;
    }

    public InstallClusterOperation(Config config) {
        super("Install Mongo cluster");
        this.config = config;

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

//        addTask(Tasks.getAptGetUpdateTask(clusterMembers));

        addTask(Tasks.getInstallMongoTask(clusterMembers));

        addTask(Tasks.getStopMongoTask(clusterMembers));

        addTask(Tasks.getRegisterIpsTask(clusterMembers, config));

        addTask(Tasks.getSetReplicaSetNameTask(
                config.getReplicaSetName(),
                config.getDataNodes()));

        addTask(Tasks.getStartConfigServersTask(
                config.getConfigServers(), config));

        addTask(Tasks.getStartRoutersTask(
                config.getRouterServers(),
                config.getConfigServers(),
                config));

        addTask(Tasks.getStartReplicaSetTask(
                config.getDataNodes(), config));

        addTask(Tasks.getRegisterSecondaryNodesWithPrimaryTask(
                config.getDataNodes(), config));

        addTask(Tasks.getRegisterReplicaSetAsShardWithRouter(
                config.getReplicaSetName(),
                config.getRouterServers().iterator().next(),
                config.getDataNodes(), config));

    }

}
