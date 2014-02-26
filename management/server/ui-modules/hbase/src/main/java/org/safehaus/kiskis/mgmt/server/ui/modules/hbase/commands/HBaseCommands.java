/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.commands;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class HBaseCommands {

//    private static final String conf = "/opt/cassandra-2.0.3/conf/cassandra.yaml";
    // INSTALLATION COMMANDS ===================================================
    public static Request getTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                HBaseModule.MODULE_NAME, //     source
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

    public static Request getSetMasterCommand(String param) {
        
        Request req = getTemplate();
        req.setProgram(HBaseCommandEnum.SET_MASTER.getProgram() + " " + param);
        return req;
    }

    public static Request getSetRegionCommand(String param) {
        
        Request req = getTemplate();
        req.setProgram(HBaseCommandEnum.SET_REGION.getProgram() + " " + param);
        return req;
    }

    public static Request getSetQuorumCommand(String param) {
        
        Request req = getTemplate();
        req.setProgram(HBaseCommandEnum.SET_QUORUM.getProgram() + " " + param);
        return req;
    }

    public static Request getSetBackupMastersCommand(String param) {
        
        Request req = getTemplate();
        req.setProgram(HBaseCommandEnum.SET_BACKUP_MASTERS.getProgram() + " " + param);
        return req;
    }

    public static Request getAptGetUpdate() {
        
        Request req = getTemplate();
        req.setProgram("apt-get update");
        return req;
    }

    public Request getCommand(HBaseCommandEnum cce) {
        
        Request req = getTemplate();
        req.setProgram(cce.getProgram());
        return req;
    }

}
