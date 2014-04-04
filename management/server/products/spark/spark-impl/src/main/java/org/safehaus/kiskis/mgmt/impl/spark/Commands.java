/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
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
        req.setProgram("service spark-all kill;apt-get --force-yes --assume-yes purge ksks-spark");
        req.setTimeout(60);
        return req;
    }

    public static Request getStartAllCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all start");
        req.setTimeout(360);
        return req;
    }

    public static Request getStopAllCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all stop");
        req.setTimeout(60);
        return req;
    }

    public static Request getStatusAllCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all status");
        req.setTimeout(60);
        return req;
    }

    public static Request getKillAllCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-all kill");
        req.setTimeout(60);
        return req;
    }

    public static Request getStartMasterCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-master start");
        req.setTimeout(90);
        return req;
    }

    public static Request getRestartMasterCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-master stop && service spark-master start");
        req.setTimeout(120);
        return req;
    }

    public static Request getStopMasterCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-master stop");
        req.setTimeout(60);
        return req;
    }

    public static Request getStatusMasterCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-master status");
        req.setTimeout(60);
        return req;
    }

    public static Request getKillMasterCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-master kill");
        req.setTimeout(60);
        return req;
    }

    public static Request getStartSlaveCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-slave start");
        req.setTimeout(90);
        return req;
    }

    public static Request getStopSlaveCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-slave stop");
        req.setTimeout(60);
        return req;
    }

    public static Request getStatusSlaveCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-slave status");
        req.setTimeout(60);
        return req;
    }

    public static Request getKillSlaveCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service spark-slave kill");
        req.setTimeout(60);
        return req;
    }

    public static Request getClearMasterIPCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && sparkMasterConf.sh clear");
        req.setTimeout(60);
        return req;
    }

    public static Request getSetMasterIPCommand(Agent masterNode) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sparkMasterConf.sh %s", masterNode.getHostname()));
        req.setTimeout(60);
        return req;
    }

    public static Request getClearSlavesCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && sparkSlaveConf.sh clear");
        req.setTimeout(60);
        return req;
    }

    public static Request getRemoveSlaveCommand(Agent slave) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sparkSlaveConf.sh clear %s", slave.getHostname()));
        req.setTimeout(60);
        return req;
    }

    public static Request getAddSlaveCommand(Agent slave) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile && sparkSlaveConf.sh %s", slave.getHostname()));
        req.setTimeout(60);
        return req;
    }

    public static Request getAddSlavesCommand(Set<Agent> slaveNodes) {
        Request req = getRequestTemplate();
//        String slaves = Arrays.toString(slaveHostnames.toArray(new String[slaveHostnames.size()])).substring(1).replaceAll("\\]$", "");
        StringBuilder slaves = new StringBuilder();
        for (Agent slaveNode : slaveNodes) {
            slaves.append(slaveNode.getHostname()).append(" ");
        }
        req.setProgram(String.format(". /etc/profile && sparkSlaveConf.sh clear ; sparkSlaveConf.sh %s", slaves));
        req.setTimeout(60);
        return req;
    }

}
