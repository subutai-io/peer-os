/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class Commands {

    public static Command getTemplate() {
        return CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                LxcModule.MODULE_NAME, //     source
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

    public static Command getCloneCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-clone");
        req.setArgs(Arrays.asList(
                "-o",
                "base-container",
                "-n",
                ""
        ));
        req.setTimeout(360);
        return cmd;
    }

    public static Command getLxcListCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-list");
        req.setTimeout(60);
        return cmd;
    }

    public static Command getLxcInfoCommand(String lxcHostname) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-info");
        req.setArgs(Arrays.asList(
                "-n",
                lxcHostname
        ));
        req.setTimeout(60);
        return cmd;
    }

    public static Command getLxcStartCommand(String lxcHostname) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-start");
        req.setArgs(Arrays.asList(
                "-n",
                lxcHostname,
                "-d"
        ));
        req.setTimeout(120);
        return cmd;
    }

    public static Command getLxcStopCommand(String lxcHostname) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-stop");
        req.setArgs(Arrays.asList(
                "-n",
                lxcHostname
        ));
        req.setTimeout(120);
        return cmd;
    }

    public static Command getLxcDestroyCommand(String lxcHostname) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/lxc-stop");
        req.setArgs(Arrays.asList(
                "-n",
                lxcHostname,
                "&&",
                "/usr/bin/lxc-destroy",
                "-n",
                lxcHostname
        ));
        req.setTimeout(180);
        return cmd;
    }

}
