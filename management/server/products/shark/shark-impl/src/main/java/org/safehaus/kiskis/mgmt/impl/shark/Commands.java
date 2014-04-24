/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.shark;

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
        return SharkImpl.getCommandRunner().createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-shark")
                .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents);
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return SharkImpl.getCommandRunner().createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-shark")
                .withTimeout(60),
                agents);
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return SharkImpl.getCommandRunner().createCommand(
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

    public static Command getSetMasterIPCommand(Set<Agent> agents, Agent masterNode) {
        return SharkImpl.getCommandRunner().createCommand(
                new RequestBuilder(String.format(". /etc/profile && sharkConf.sh clear master ; sharkConf.sh master %s", masterNode.getHostname()))
                .withTimeout(60),
                agents);
    }

}
