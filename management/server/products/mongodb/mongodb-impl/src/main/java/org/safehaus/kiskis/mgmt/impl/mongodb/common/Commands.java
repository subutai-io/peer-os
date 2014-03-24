/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb.common;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.Timeouts;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class Commands {

    // INSTALLATION COMMANDS ===================================================
    public static Request getTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                Config.PRODUCT_KEY, //     source
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

    //execute on each selected lxc node
    public static Request getInstallCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "install",
                "ksks-mongo"
        ));
        req.setTimeout(360);
        return req;
    }

    //execute on each selected lxc node
    public static Request getAptGetUpdateCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "update"
        ));
        req.setStdOut(OutputRedirection.NO);
        req.setTimeout(180);
        return req;
    }

    //execute on each replica
    public static Request getSetReplicaSetNameCommand(String replicaSetName) {

        Request req = getTemplate();
        req.setProgram("/bin/sed");
        req.setArgs(Arrays.asList(
                "-i",
                String.format("'s/# replSet = setname/replSet = %s/1'", replicaSetName),//replace placeholder with actual data
                String.format("'%s'", Constants.DATA_NODE_CONF_FILE)
        ));
        req.setTimeout(30);
        return req;
    }

    //execute for each replica adding info about each of the other replicas
    public static Request getAddNodesIpHostToOtherNodesCommand(String ipHostPair) {

        Request req = getTemplate();
        req.setProgram(ipHostPair);
        req.setTimeout(30);
        return req;
    }

    //execute on primary replica
    public static Request getRegisterSecondaryNodesWithPrimaryCommand(String secondaryNodes, Config cfg) {

        Request req = getTemplate();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--port",
                cfg.getDataNodePort() + "",
                "--eval",
                "\"rs.initiate();\"",
                ";sleep 30;",
                "mongo",
                "--port",
                cfg.getDataNodePort() + "",
                "--eval",
                String.format("\"%s\"", secondaryNodes)
        ));
        req.setTimeout(180);
        return req;
    }

    //execute on primary data node
    public static Request getUnregisterSecondaryNodeFromPrimaryCommand(String host, Config cfg) {

        Request req = getTemplate();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--port",
                cfg.getDataNodePort() + "",
                "--eval",
                String.format("\"rs.remove('%s:%s');\"", host, cfg.getDataNodePort())
        ));
        req.setTimeout(30);
        return req;
    }

    //execute on any router member
    public static Request getRegisterShardsWithRouterCommand(String shards, Config cfg) {

        Request req = getTemplate();
        req.setProgram("sleep 30;");
        req.setArgs(Arrays.asList(
                "mongo",
                "--port",
                cfg.getRouterPort() + "",
                "--eval",
                String.format("\"%s\"", shards)
        ));
        req.setTimeout(180);
        return req;
    }

    // LIFECYCLE COMMANDS =======================================================
    //execute on config server
    public static Request getStartConfigServerCommand(Config cfg) {

        Request req = getTemplate();
        req.setProgram("/bin/mkdir");
        req.setArgs(Arrays.asList(
                "-p",
                Constants.CONFIG_DIR,
                ";",
                "mongod",
                "--configsvr",
                "--dbpath",
                Constants.CONFIG_DIR,
                "--port",
                cfg.getCfgSrvPort() + "", // this might be user-supplied
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        //                        "--logappend"
        ));
        req.setTimeout(180);
        return req;
    }

    //execute on router
    public static Request getStartRouterCommand(String configServersArg, Config cfg) {

        Request req = getTemplate();
        req.setProgram("mongos");
        req.setArgs(Arrays.asList(
                "--configdb",
                configServersArg,
                "--port",
                cfg.getRouterPort() + "",
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        ));
        req.setTimeout(180);
        return req;
    }

    public static Request getStartRouterCommand2(String configServersArg, Config cfg) {

        Request req = getTemplate();
        req.setProgram("mongos");
        req.setArgs(Arrays.asList(
                "--configdb",
                configServersArg,
                "--port",
                cfg.getRouterPort() + "",
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        ));
        req.setTimeout(60);
        return req;
    }

    //execute on shard
    public static Request getStartNodeCommand(Config cfg) {

        Request req = getTemplate();
        req.setProgram("mongod");
        req.setArgs(Arrays.asList(
                "--config",
                Constants.DATA_NODE_CONF_FILE,
                "--port",
                cfg.getDataNodePort() + "",
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        ));
        req.setTimeout(300);
        return req;
    }

    //execute on any cluster member
    public static Request getCheckInstanceRunningCommand(String host, String port) {

        Request req = getTemplate();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--host",
                host,
                "--port",
                port
        ));
        req.setTimeout(Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC);
        return req;
    }

    public static Request getCheckConfigSrvStatusCommand(String host, Config cfg) {
        return getCheckInstanceRunningCommand(host, cfg.getCfgSrvPort() + "");
    }

    public static Request getCheckRouterStatusCommand(String host, Config cfg) {
        return getCheckInstanceRunningCommand(host, cfg.getRouterPort() + "");
    }

    public static Request getCheckDataNodeStatusCommand(String host, Config cfg) {
        return getCheckInstanceRunningCommand(host, cfg.getDataNodePort() + "");
    }

    // RECONFIGURATION COMMANDS
    public static Request getStopNodeCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/pkill");
        req.setArgs(Arrays.asList(
                "-2",
                "mongo"
        ));
        req.setTimeout(Timeouts.STOP_NODE_STATUS_TIMEOUT_SEC);
        return req;
    }

    public static Request getFindPrimaryNodeCommand(Config cfg) {

        Request req = getTemplate();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                "'db.isMaster()'",
                "|",
                "mongo",
                "--port",
                cfg.getDataNodePort() + ""
        ));
        req.setTimeout(30);
        return req;
    }
}
