/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mahout;

import java.util.Set;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 * @author dilshat
 */
public class Commands {

    public static Command getInstallCommand(Set<Agent> agents) {
        return MahoutImpl.getCommandRunner().createCommand(
                "Install Mahout",
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-mahout")
                .withTimeout(90),
                agents);
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return MahoutImpl.getCommandRunner().createCommand(
                "Uninstall Mahout",
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-mahout")
                .withTimeout(60),
                agents);
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return MahoutImpl.getCommandRunner().createCommand(
                "Check installed ksks packages",
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

}
