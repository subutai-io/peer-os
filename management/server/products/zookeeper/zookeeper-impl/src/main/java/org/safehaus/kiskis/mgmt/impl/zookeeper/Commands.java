/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.zookeeper;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

/**
 *
 * @author dilshat
 */
public class Commands {

    public static Command getInstallCommand(Set<Agent> agents) {
        return Impl.getCommandRunner().createCommand(
                new RequestBuilder("sleep 10 ; apt-get --force-yes --assume-yes install ksks-zookeeper")
                .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents);
    }

    public static Command getStartCommand(Set<Agent> agents) {
        return Impl.getCommandRunner().createCommand(
                new RequestBuilder("service zookeeper start").withTimeout(15),
                agents);
    }

    public static Command getRestartCommand(Set<Agent> agents) {
        return Impl.getCommandRunner().createCommand(
                new RequestBuilder("service zookeeper restart").withTimeout(15),
                agents);
    }

    public static Command getStopCommand(Agent agent) {
        return Impl.getCommandRunner().createCommand(
                new RequestBuilder("service zookeeper stop"),
                Util.wrapAgentToSet(agent));
    }

    public static Command getStatusCommand(Agent agent) {
        return Impl.getCommandRunner().createCommand(
                new RequestBuilder("service zookeeper status"),
                Util.wrapAgentToSet(agent));
    }

    public static Command getUpdateSettingsCommand(String zkName, Set<Agent> agents) {
        StringBuilder zkNames = new StringBuilder();
        for (int i = 1; i <= agents.size(); i++) {
            zkNames.append(zkName).append(i).append(" ");
        }

        Set<AgentRequestBuilder> requestBuilders = new HashSet<AgentRequestBuilder>();

        int id = 0;
        for (Agent agent : agents) {
            requestBuilders.add(new AgentRequestBuilder(agent,
                    String.format(". /etc/profile && zookeeper-conf.sh %s && zookeeper-setID.sh %s", zkNames, ++id)));
        }

        return Impl.getCommandRunner().createCommand(requestBuilders);
    }

}
