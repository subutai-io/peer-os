/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
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
                MongoModule.MODULE_NAME, //     source
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

    //execute on each selected lxc node
    public static Request getUninstallCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "purge",
                "ksks-mongo"
        ));
        req.setTimeout(90);
        return req;
    }

    //execute on each selected lxc node
    public static Request getKillAllCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/pkill");
        req.setArgs(Arrays.asList(
                "-9",
                "-f",
                //                "'mongod|ksks-mongo|mongos'"
                "'mongod|mongos'"
        ));
        req.setTimeout(30);
        return req;
    }

    //execute on each selected lxc node
    public static Request getCleanCommand() {

        Request req = getTemplate();
        req.setProgram("/bin/rm -R");
        req.setArgs(Arrays.asList(
                Constants.MONGO_DIR,
                "&",
                "/bin/rm -R",
                Constants.CONFIG_DIR,
                "&",
                "/bin/rm",
                Constants.DATA_NODE_CONF_FILE,
                "&",
                "/bin/rm -R",
                Constants.LOG_DIR
        ));
        req.setTimeout(30);
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

    //execute on any one replica
    public static Request getFindReplicaSetMasterCommand() {

        Request req = getTemplate();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                "'rs.status()'",
                "|",
                "mongo"
        //output will contain json object containing property [members] 
        //which is json array with info on each replica where primary 
        //replica has property ["stateStr" : "PRIMARY"] 
        //and hostname is ["name" : "mongoTestShard1:27017"]
        ));
        req.setTimeout(30);
        return req;
    }

    //execute on primary replica
    public static Request getRegisterSecondaryNodesWithPrimaryCommand(String secondaryNodes) {

        Request req = getTemplate();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--port",
                Constants.DATA_NODE_PORT + "",
                "--eval",
                "\"rs.initiate();\"",
                ";sleep 30;",
                "mongo",
                "--port",
                Constants.DATA_NODE_PORT + "",
                "--eval",
                String.format("\"%s\"", secondaryNodes)
        ));
        req.setTimeout(180);
        return req;
    }

    //execute on primary data node
    public static Request getUnregisterSecondaryNodeFromPrimaryCommand(String host) {

        Request req = getTemplate();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--port",
                Constants.DATA_NODE_PORT + "",
                "--eval",
                String.format("\"rs.remove('%s:%s');\"", host, Constants.DATA_NODE_PORT)
        ));
        req.setTimeout(30);
        return req;
    }

    //execute on any router member
    public static Request getRegisterShardsWithRouterCommand(String shards) {

        Request req = getTemplate();
        req.setProgram("sleep 30;");
        req.setArgs(Arrays.asList(
                "mongo",
                "--port",
                Constants.ROUTER_PORT + "",
                "--eval",
                String.format("\"%s\"", shards)
        ));
        req.setTimeout(180);
        return req;
    }

    // LIFECYCLE COMMANDS =======================================================
    //execute on config server
    public static Request getStartConfigServerCommand() {

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
                Constants.CONFIG_SRV_PORT + "", // this might be user-supplied
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        //                        "--logappend"
        ));
        req.setTimeout(180);
        return req;
    }

    //execute on router
    public static Request getStartRouterCommand(String configServersArg) {

        Request req = getTemplate();
        req.setProgram("mongos");
        req.setArgs(Arrays.asList(
                "--configdb",
                configServersArg,
                "--port",
                Constants.ROUTER_PORT + "",
                "--fork",
                "--logpath",
                String.format("%s/mongodb.log", Constants.LOG_DIR)
        ));
        req.setTimeout(180);
        return req;
    }

    //execute on shard
    public static Request getStartNodeCommand() {

        Request req = getTemplate();
        req.setProgram("mongod");
        req.setArgs(Arrays.asList(
                "--config",
                Constants.DATA_NODE_CONF_FILE,
                "--port",
                Constants.DATA_NODE_PORT + "",
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
        req.setTimeout(10);
        return req;
    }

    public static Request getCheckConfigSrvStatusCommand(String host) {
        return getCheckInstanceRunningCommand(host, Constants.CONFIG_SRV_PORT + "");
    }

    public static Request getCheckRouterStatusCommand(String host) {
        return getCheckInstanceRunningCommand(host, Constants.ROUTER_PORT + "");
    }

    public static Request getCheckDataNodeStatusCommand(String host) {
        return getCheckInstanceRunningCommand(host, Constants.DATA_NODE_PORT + "");
    }

    // RECONFIGURATION COMMANDS
    public static Request getStopNodeCommand() {

        Request req = getTemplate();
        req.setProgram("/usr/bin/pkill");
        req.setArgs(Arrays.asList(
                "-2",
                "mongo"
        ));
        req.setTimeout(30);
        return req;
    }

    public static Request getFindPrimaryNodeCommand() {

        Request req = getTemplate();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                "'db.isMaster()'",
                "|",
                "mongo",
                "--port",
                Constants.DATA_NODE_PORT + ""
        ));
        req.setTimeout(30);
        return req;
    }
}
