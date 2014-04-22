/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.oozie;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class Commands {

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                OozieImpl.MODULE_NAME, //     source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //  
    }

    public static Request getInstallServerCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes install ksks-oozie-server");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallServerCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-oozie-server");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getInstallClientCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes install ksks-oozie-client");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallClientCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-oozie-client");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getConfigureRootHostsCommand(String ip) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.hosts " + ip);
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getConfigureRootGroupsCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser.root.groups '\\*' ");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getStartServerCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service oozie-server start");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getStopServerCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service oozie-server stop");
        return req;
    }

    public static Request getStatusServerCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service oozie status");
        return req;
    }


}
