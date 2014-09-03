/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.mahout;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.enums.OutputRedirection;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {
        return createCommand(
                "Install Mahout",
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-mahout")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return createCommand(
                "Uninstall Mahout",
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-mahout")
                        .withTimeout(60),
                agents
        );
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return createCommand(
                "Check installed ksks packages",
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

}
