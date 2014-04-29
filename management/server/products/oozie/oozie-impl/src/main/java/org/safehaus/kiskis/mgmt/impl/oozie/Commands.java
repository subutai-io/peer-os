/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.oozie;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

//    public static Request getRequestTemplate() {
//        return CommandFactory.newRequest(
//                RequestType.EXECUTE_REQUEST, // type
//                null, //                        !! agent uuid
//                OozieImpl.MODULE_NAME, //     source
//                null, //                        !! task uuid
//                1, //                           !! request sequence number
//                "/", //                         cwd
//                "pwd", //                        program
//                OutputRedirection.RETURN, //    std output redirection
//                OutputRedirection.RETURN, //    std error redirection
//                null, //                        stdout capture file path
//                null, //                        stderr capture file path
//                "root", //                      runas
//                null, //                        arg
//                null, //                        env vars
//                30); //
//    }
//
//    public static Request getInstallServerCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("apt-get --force-yes --assume-yes install ksks-oozie-server");
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getUninstallServerCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("apt-get --force-yes --assume-yes purge ksks-oozie-server");
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getInstallClientCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("apt-get --force-yes --assume-yes install ksks-oozie-client");
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getUninstallClientCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("apt-get --force-yes --assume-yes purge ksks-oozie-client");
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getConfigureRootHostsCommand(String ip) {
//        Request req = getRequestTemplate();
//        req.setProgram(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.hosts " + ip);
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getConfigureRootGroupsCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.groups '\\*' ");
//        req.setStdOut(OutputRedirection.NO);
//        req.setTimeout(90);
//        return req;
//    }
//
//    public static Request getStartServerCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("service oozie-server start");
//        req.setStdOut(OutputRedirection.NO);
//        return req;
//    }
//
//    public static Request getStopServerCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("service oozie-server stop");
//        return req;
//    }
//
//    public static Request getStatusServerCommand() {
//        Request req = getRequestTemplate();
//        req.setProgram("service oozie status");
//        return req;
//    }

    public static Command getInstallServerCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        "sleep 10; apt-get --force-yes --assume-yes install oozie-ksks-server")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getInstallClientCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        "sleep 10; apt-get --force-yes --assume-yes install oozie-ksks-client")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getStartServerCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service oozie-server start")
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
                        "apt-get --force-yes --assume-yes purge oozie-ksks-server")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallClientsCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "sleep 10; apt-get --force-yes --assume-yes purge oozie-ksks-server")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }
}
