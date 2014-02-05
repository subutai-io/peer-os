/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mahout;

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
                Mahout.MODULE_NAME, //     source
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

    public static Request getCheckCommand() {
        Request req = getRequestTemplate();
        req.setProgram("dpkg -l | grep '^ii' | grep ksks");
        return req;
    }

    public static Request getUninstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-mahout");
        req.setTimeout(60);
        return req;
    }

    public static Request getInstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get update && apt-get --force-yes --assume-yes install ksks-mahout");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }
}
