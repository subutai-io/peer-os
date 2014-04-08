package org.safehaus.kiskis.mgmt.impl.hadoop;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.Arrays;

/**
 * Created by daralbaev on 02.04.14.
 */
public class Commands {
    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                HadoopImpl.MODULE_NAME, //     source
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
                30); //
    }

    public static Request getInstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                "sleep 10;" +
                        "apt-get update && " +
                        "apt-get --force-yes --assume-yes install ksks-hadoop"
        );
        req.setTimeout(180);
        return req;
    }

    public static Request getClearMastersCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop-master-slave.sh masters clear"
        );
        return req;
    }

    public static Request getClearSlavesCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop-master-slave.sh slaves clear"
        );
        return req;
    }

    public static Request getSetMastersCommand(Config cfg) {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop-configure.sh"
        );
        req.setArgs(Arrays.asList(
                String.format("%s:%d", cfg.getNameNode().getHostname(), Config.NAME_NODE_PORT),
                String.format("%s:%d", cfg.getJobTracker().getHostname(), Config.JOB_TRACKER_PORT),
                String.format("%d", cfg.getReplicationFactor())
        ));
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getAddSecondaryNamenodeCommand(Config cfg) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                ". /etc/profile && " +
                        "hadoop-master-slave.sh masters %s",
                cfg.getSecondaryNameNode().getHostname()
        ));
        return req;
    }

    public static Request getAddSlaveCommand(String host) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                ". /etc/profile && " +
                        "hadoop-master-slave.sh slaves %s", host
        ));
        return req;
    }

    public static Request getFormatNameNodeCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop namenode -format"
        );
        return req;
    }

    public static Request getNameNodeCommand(String command) {
        Request req = getRequestTemplate();
        req.setProgram(
                String.format("service hadoop-dfs %s", command)
        );
        req.setTimeout(20);
        return req;
    }

    public static Request getJobTrackerCommand(String command) {
        Request req = getRequestTemplate();
        req.setProgram(
                String.format("service hadoop-mapred %s", command)
        );
        req.setTimeout(20);
        return req;
    }
}
