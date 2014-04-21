package org.safehaus.kiskis.mgmt.impl.networkmanager;

import org.safehaus.kiskis.mgmt.api.networkmanager.NetworkManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;

/**
 * Created by daralbaev on 04.04.14.
 */
public class NetwokManagerImpl implements NetworkManager {
    private TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Override
    public boolean configSshOnAgents(List<Agent> agentList) {
        return new SshManager(taskRunner, agentList).execute();
    }

    @Override
    public boolean configSshOnAgents(List<Agent> agentList, Agent agent) {
        return new SshManager(taskRunner, agentList).execute(agent);
    }

    @Override
    public boolean configHostsOnAgents(List<Agent> agentList, String domainName) {
        return new HostManager(taskRunner, agentList, domainName).execute();
    }


}
