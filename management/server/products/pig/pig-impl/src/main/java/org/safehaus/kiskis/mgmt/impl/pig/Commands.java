/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.pig;

import java.util.Set;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

/**
 * @author dilshat
 */
public class Commands {

    public static Command getInstallCommand(Set<Agent> agents) {
        return PigImpl.getCommandRunner().createCommand(
                "Install Pig",
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-pig")
                .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents);
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return PigImpl.getCommandRunner().createCommand(
                "Uninstall Pig",
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-pig")
                .withTimeout(60),
                agents);
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return PigImpl.getCommandRunner().createCommand(
                "Check installed ksks packages",
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

}
