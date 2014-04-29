/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.pig;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-pig")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-pig")
                        .withTimeout(60),
                agents
        );
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

}
