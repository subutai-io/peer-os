/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.hbase;

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
                HBaseImpl.MODULE_NAME, //     source
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
        req.setProgram("apt-get --force-yes --assume-yes install ksks-hbase");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-hbase");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request setMasterCommand(String hadoopNameNode, String master) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HBASE_HOME/scripts/master.sh " + hadoopNameNode + " " + master);
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request setRegionCommand(String regions) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HBASE_HOME/scripts/region.sh " + regions);
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request setQuorumCommand(String quorums) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HBASE_HOME/scripts/quorum.sh " + quorums);
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request setBackupMasterCommand(String backupMaster) {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && $HBASE_HOME/scripts/backUpMasters.sh " + backupMaster);
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service hbase start &");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getStopCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service hbase stop &");
        return req;
    }

    public static Request getStatusCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service hbase status");
        return req;
    }


}
