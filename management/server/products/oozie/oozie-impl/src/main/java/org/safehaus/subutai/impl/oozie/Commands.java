/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.oozie;

import java.util.Set;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandsSingleton;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.enums.OutputRedirection;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    private AgentManager agentManager;


    public Commands( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public static Command getInstallServerCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-server")
                        .withTimeout(180)
                        .withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getInstallClientCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-client")
                        .withTimeout(180)
                        .withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getStartServerCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service oozie-server start &")
                ,
                agents
        );
    }

    public static Command getStopServerCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service oozie-server stop")
                ,
                agents
        );
    }

    public static Command getStatusServerCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service oozie-server status")
                ,
                agents
        );
    }

    public static Command getConfigureRootHostsCommand(Set<Agent> agents, String param) {

        return createCommand(
                new RequestBuilder(
                        String.format(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.hosts %s", param))
                ,
                agents
        );
    }

    public static Command getConfigureRootGroupsCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        String.format(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.groups '\\*' "))
                ,
                agents
        );
    }

    public static Command getUninstallServerCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "apt-get --force-yes --assume-yes purge ksks-oozie-server")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallClientsCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "apt-get --force-yes --assume-yes purge ksks-oozie-client")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }
}
