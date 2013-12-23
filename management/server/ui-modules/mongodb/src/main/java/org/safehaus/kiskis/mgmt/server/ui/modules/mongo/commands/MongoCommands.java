/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class MongoCommands {

    public static Command getTemplate() {
        return (Command) CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                MongoModule.MODULE_NAME, //     source
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
                30); //                        timeout (sec)
    }

    public static Command getInstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "install",
                "ksks-mongo"
        ));
        req.setTimeout(180);
        return cmd;
    }

    public static Command getUninstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "purge",
                "ksks-mongo"
        ));
        req.setTimeout(180);
        return cmd;
    }

    public static Command getCheckCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--version"
        ));
        return cmd;
    }

    public static Command getStartConfigServerCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/mkdir");
        req.setArgs(Arrays.asList(
                "/data/configdb",
                "&&",
                "mongod",
                "--configsvr",
                "--dbpath",
                "/data/configdb",
                "--port",
                "27019" // this might be user-supplied
        ));
        req.setTimeout(180);
        return cmd;
    }

    public static Command getStartRouterCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongos");
        req.setArgs(Arrays.asList(
                "--configdb"
        //add config servers(with ports) based on user selection comma-separated
        ));
        req.setTimeout(180);
        return cmd;
    }
}
