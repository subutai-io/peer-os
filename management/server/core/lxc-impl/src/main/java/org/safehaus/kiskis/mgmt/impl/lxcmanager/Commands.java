/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class Commands {

    public static Request getTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                null, //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //                         timeout (sec)
    }

    public static Request getCloneCommand() {
        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-clone -o base-container -n ");
        req.setTimeout(360);
        return req;
    }

    public static Request getLxcListCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-list");
        req.setTimeout(60);
        return req;
    }

    public static Request getLxcInfoCommand(String lxcHostname) {

        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-info -n " + lxcHostname);
        req.setTimeout(60);
        return req;
    }

    public static Request getLxcInfoWithWaitCommand(String lxcHostname) {

        Request req = getTemplate();
        req.setProgram("sleep 5;/usr/bin/lxc-info -n " + lxcHostname);
        req.setTimeout(60);
        return req;
    }

    public static Request getLxcStartCommand(String lxcHostname) {

        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-start -n " + lxcHostname + " -d");
        req.setTimeout(120);
        return req;
    }

    public static Request getLxcStopCommand(String lxcHostname) {

        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-stop -n " + lxcHostname);
        req.setTimeout(120);
        return req;
    }

    public static Request getLxcDestroyCommand(String lxcHostname) {

        Request req = getTemplate();
        req.setProgram("/usr/bin/lxc-stop -n " + lxcHostname + " && /usr/bin/lxc-destroy -n " + lxcHostname);
        req.setTimeout(180);
        return req;
    }

    public static Request getMetricsCommand() {

        Request req = getTemplate();
        req.setProgram("free -m | grep buffers/cache ; df -h /dev/sda1 | grep /dev/sda1 ; uptime ; nproc");
        req.setTimeout(30);
        return req;
    }

}
