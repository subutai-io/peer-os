/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.accumulo;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-accumulo")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-accumulo")
                        .withTimeout(60),
                agents
        );
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

    public static Command getStartCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("/etc/init.d/accumulo start").withTimeout(60),
                agents);
    }

    public static Command getStopCommand(Agent agent) {
        return createCommand(
                new RequestBuilder("/etc/init.d/accumulo stop"),
                Util.wrapAgentToSet(agent));
    }

    public static Command getStatusCommand(Agent agent) {
        return createCommand(
                new RequestBuilder("/etc/init.d/accumulo status"),
                Util.wrapAgentToSet(agent));
    }

    public static Command getAddMasterCommand(Set<Agent> nodes, Agent masterNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh masters add %s", masterNode.getHostname())),
                nodes);
    }

    public static Command getClearMasterCommand(Set<Agent> nodes, Agent masterNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh masters clear %s", masterNode.getHostname())),
                nodes);
    }

    public static Command getClearAllMastersCommand(Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(". /etc/profile && accumuloMastersConf.sh masters clear"),
                nodes);
    }

    public static Command getAddTracersCommand(Set<Agent> nodes, Set<Agent> tracerNodes) {
        StringBuilder tracersSpaceSeparated = new StringBuilder();
        for (Agent tracer : tracerNodes) {
            tracersSpaceSeparated.append(tracer.getHostname()).append(" ");
        }
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh tracers add %s", tracersSpaceSeparated)),
                nodes);
    }

    public static Command getClearTracerCommand(Set<Agent> nodes, Agent tracerNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh tracers clear %s", tracerNode.getHostname())),
                nodes);
    }

    public static Command getClearAllTracersCommand(Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(". /etc/profile && accumuloMastersConf.sh tracers clear"),
                nodes);
    }

    public static Command getAddGCCommand(Set<Agent> nodes, Agent gcNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh gc add %s", gcNode.getHostname())),
                nodes);
    }

//    public static Command getClearGCCommand(Set<Agent> nodes, Agent gcNode) {
//        return createCommand(
//                new RequestBuilder(String.format(". /etc/profile && accumuloMastersConf.sh gc clear %s", gcNode.getHostname())),
//                nodes);
//    }

    public static Command getClearAllGCsCommand(Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(". /etc/profile && accumuloMastersConf.sh gc clear"),
                nodes);
    }

    public static Command getAddSlavesCommand(Set<Agent> nodes, Set<Agent> slaveNodes) {
        StringBuilder slavesSpaceSeparated = new StringBuilder();
        for (Agent tracer : slaveNodes) {
            slavesSpaceSeparated.append(tracer.getHostname()).append(" ");
        }
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloSlavesConf.sh slaves add %s", slavesSpaceSeparated)),
                nodes);
    }

    public static Command getClearSlaveCommand(Set<Agent> nodes, Agent slaveNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumuloSlavesConf.sh slaves clear %s", slaveNode.getHostname())),
                nodes);
    }

    public static Command getClearAllSlavesCommand(Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(". /etc/profile && accumuloSlavesConf.sh slaves clear"),
                nodes);
    }

    public static Command getAddPropertyCommand(String propertyName, String propertyValue, Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumulo-property.sh add %s %s", propertyName, propertyValue)),
                nodes);
    }

    public static Command getClearPropertyCommand(String propertyName, Set<Agent> nodes) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && accumulo-property.sh clear %s", propertyName)),
                nodes);
    }
}
