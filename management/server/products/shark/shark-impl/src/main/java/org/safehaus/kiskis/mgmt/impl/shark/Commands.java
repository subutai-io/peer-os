/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.shark;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * @author dilshat
 */
public class Commands {

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        source
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

    public static Request getCheckInstalledCommand() {
        Request req = getRequestTemplate();
        req.setProgram("dpkg -l | grep '^ii' | grep ksks");
        return req;
    }

    public static Request getInstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes install ksks-shark");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-shark");
        req.setTimeout(60);
        return req;
    }

    public static Request getSetMasterIPCommand(Agent masterNode) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sharkConf.sh clear master ; sharkConf.sh master %s", masterNode.getHostname()));
        req.setTimeout(60);
        return req;
    }

}
