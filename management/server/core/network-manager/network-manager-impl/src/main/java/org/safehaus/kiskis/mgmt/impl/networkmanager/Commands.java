package org.safehaus.kiskis.mgmt.impl.networkmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * Created by daralbaev on 03.04.14.
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

    public static Request getCreateSSHCommand() {
        Request req = getTemplate();
        req.setProgram(
                "rm -Rf /root/.ssh && " +
                        "mkdir -p /root/.ssh && " +
                        "chmod 700 /root/.ssh && " +
                        "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa"
        );
        return req;
    }

    public static Request getReadSSHCommand() {
        Request req = getTemplate();
        req.setProgram("cat /root/.ssh/id_dsa.pub");
        return req;
    }

    public static Request getWriteSSHCommand(String key) {
        Request req = getTemplate();
        req.setProgram(String.format(
                "mkdir -p /root/.ssh && " +
                        "chmod 700 /root/.ssh && " +
                        "echo '%s' >> /root/.ssh/authorized_keys && " +
                        "chmod 644 /root/.ssh/authorized_keys", key
        ));
        return req;
    }

    public static Request getConfigSSHCommand() {
        Request req = getTemplate();
        req.setProgram(
                "echo 'Host *' > /root/.ssh/config && " +
                        "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                        "chmod 644 /root/.ssh/config"
        );
        return req;
    }

    public static Request getReadHostsCommand() {
        Request req = getTemplate();
        req.setProgram(
                "cat /etc/hosts"
        );
        return req;
    }

    public static Request getWriteHostsCommand(String hosts) {
        Request req = getTemplate();
        req.setProgram(
                String.format("echo '%s' > /etc/hosts", hosts)
        );
        return req;
    }
}
