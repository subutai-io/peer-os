/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.zookeeper;

import java.util.Set;
import static org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.id;
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
                null, //     source
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
        req.setProgram("sleep 10 ; apt-get --force-yes --assume-yes install ksks-zookeeper");
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(90);
        return req;
    }

    public static Request getStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service zookeeper start");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getReStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service zookeeper restart");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getStopCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service zookeeper stop");
        return req;
    }

    public static Request getStatusCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service zookeeper status");
        return req;
    }

    public static Request getCatCfgFileCommand() {
        Request req = getRequestTemplate();
        req.setProgram("cat /opt/zookeeper-3.4.5/zoo.cfg");
        return req;
    }

    public static Request getUpdateCfgFileCommand(Set<Agent> nodes) {
        Request req = getRequestTemplate();
        StringBuilder nodesStr = new StringBuilder();
        for (Agent node : nodes) {
            nodesStr.append(node.getHostname()).append(" ");
        }
        req.setProgram(String.format(". /etc/profile & zookeeper-conf.sh %s", nodesStr));
        return req;
    }

    public static Request getSetZKIdCommand(String id) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(". /etc/profile & zookeeper-setID.sh %s", id));
        return req;
    }

}
