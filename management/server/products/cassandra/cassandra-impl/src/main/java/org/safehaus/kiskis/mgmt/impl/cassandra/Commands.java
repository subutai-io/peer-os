/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.cassandra;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        "sleep 10; apt-get --force-yes --assume-yes install ksks-cassandra")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getStartCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra start")
                ,
                agents
        );
    }

    public static Command getStopCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra stop")
                ,
                agents
        );
    }

    public static Command getStatusCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra status")
                ,
                agents
        );
    }

    public static Command getConfigureCommand(Set<Agent> agents, String param) {

        return createCommand(
                new RequestBuilder(
                        String.format(". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s", param))
                ,
                agents
        );
    }

    public static Command getConfigureRpcAndListenAddressesCommand(Set<Agent> agents, String param) {
        Set<AgentRequestBuilder> sarb = new HashSet<AgentRequestBuilder>();
        for (Agent agent : agents) {
            AgentRequestBuilder arb = new AgentRequestBuilder(agent,
                    String.format(". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s %s ",
                            param,
                            Util.getAgentIpByMask(agent, Common.IP_MASK))
            );
            sarb.add(arb);
        }
        return createCommand(sarb);
    }

}
