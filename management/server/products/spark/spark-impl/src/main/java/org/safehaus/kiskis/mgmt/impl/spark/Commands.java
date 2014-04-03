/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

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
        req.setProgram("sleep 10; apt-get --force-yes --assume-yes install ksks-spark");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getUninstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("apt-get --force-yes --assume-yes purge ksks-spark");
        req.setTimeout(60);
        return req;
    }

    public static Request getStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all start");
        req.setTimeout(60);
        return req;
    }

    public static Request getStopCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all stop");
        req.setTimeout(60);
        return req;
    }

    public static Request getStatusCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all status");
        req.setTimeout(60);
        return req;
    }

    public static Request getKillCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all kill");
        req.setTimeout(60);
        return req;
    }

    public static Request getClearMasterIPCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && sparkMasterConf.sh clear");
        req.setTimeout(60);
        return req;
    }

    public static Request getSetMasterIPCommand(String masterHostname) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sparkMasterConf.sh %s", masterHostname));
        req.setTimeout(60);
        return req;
    }

    public static Request getClearSlavesCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && sparkSlaveConf.sh clear");
        req.setTimeout(60);
        return req;
    }

    public static Request getRemoveSlaveCommand(String slaveHostname) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sparkSlaveConf.sh clear %s", slaveHostname));
        req.setTimeout(60);
        return req;
    }

    public static Request getAddSlaveCommand(String slaveHostname) {
        Request req = getRequestTemplate();
        req.setProgram(String.format("sparkSlaveConf.sh %s", slaveHostname));
        req.setTimeout(60);
        return req;
    }

}
