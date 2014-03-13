package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands;

import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class OozieCommands {

//    private static final String conf = "/opt/cassandra-2.0.3/conf/cassandra.yaml";
    // INSTALLATION COMMANDS ===================================================
    public static Request getTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                OozieModule.MODULE_NAME, //     source
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
                120); //                        timeout (sec)
    }

    public Request getSetRootHost(String ip) {
        Request req = getTemplate();
        req.setProgram(OozieCommandEnum.CONFIGURE_ROOT_HOST.getProgram() + " " + ip);
        return req;
    }
    
    public Request getSetRootGroups() {
        Request req = getTemplate();
        req.setProgram(OozieCommandEnum.CONFIGURE_ROOT_HOST.getProgram());
        return req;
    }

    public Request getAptGetUpdate() {
        Request req = getTemplate();
        req.setProgram("apt-get update");
        return req;
    }

    public Request getCommand(OozieCommandEnum cce) {
        Request req = getTemplate();
        req.setProgram(cce.getProgram());
        return req;
    }

}
