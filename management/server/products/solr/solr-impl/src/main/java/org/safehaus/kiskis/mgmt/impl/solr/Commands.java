/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.solr;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands {

    private static Commands INSTANCE;
    private CommandRunner commandRunner;

    private Commands() {
    }

    public static void init(CommandRunner commandRunner) {
        INSTANCE = new Commands();
        INSTANCE.commandRunner = commandRunner;
    }


    private static CommandRunner getCommandRunner() {
        if (INSTANCE == null) {
            throw new RuntimeException("Commands not initialized");
        }
        return INSTANCE.commandRunner;
    }

    public static Command getInstallCommand(Set<Agent> agents) {
        return getCommandRunner().createCommand(
                new RequestBuilder("sleep 10 ; apt-get --force-yes --assume-yes install ksks-solr")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getStartCommand(Agent agent) {
        return getCommandRunner().createCommand(
                new RequestBuilder("service solr start").withStdOutRedirection(OutputRedirection.NO),
                Util.wrapAgentToSet(agent));
    }

    public static Command getStopCommand(Agent agent) {
        return getCommandRunner().createCommand(
                new RequestBuilder("service solr stop"),
                Util.wrapAgentToSet(agent));
    }

    public static Command getStatusCommand(Agent agent) {
        return getCommandRunner().createCommand(
                new RequestBuilder("service solr status"),
                Util.wrapAgentToSet(agent));
    }

}
