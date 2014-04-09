package org.safehaus.kiskis.mgmt.impl.flume;

import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

public class Commands {

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                Config.PRODUCT_KEY, //     source
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

    public static Request getInstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("sleep 10; apt-get update && apt-get --force-yes --assume-yes install ksks-flume");
        req.setStdOut(OutputRedirection.NO); // TODO:
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-flume");
        req.setTimeout(60);
        return req;
    }

    public static Request getStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service flume-ng start agent");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getStopCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service flume-ng stop agent");
        return req;
    }

    public static Request getStatusCommand() {
        Request req = getRequestTemplate();
        // TODO: 'ksks-flume' may be used
        req.setProgram("dpkg -l | grep ksks");
        return req;
    }

}
