/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class MongoCommands {

    // INSTALLATION COMMANDS ===================================================
    public static Command getTemplate() {
        return (Command) CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        !! source
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
                30); //                        timeout (sec)
    }

    //execute on each selected lxc node
    public static Command getInstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "update",
                "&&",
                "/usr/bin/apt-get",
                "--force-yes",
                "--assume-yes",
                "install",
                "ksks-mongo"
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on each selected lxc node
    public static Command getUninstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "purge",
                "ksks-mongo"
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on each replica
    public static Command getSetReplicaSetNameCommand(String replicaSetName) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/sed");
        req.setArgs(Arrays.asList(
                "-i",
                String.format("'s/# replSet = setname/replSet = %s/1'", replicaSetName),//replace placeholder with actual data
                "'/etc/mongodb.conf'"
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute for each replica adding info about each of the other replicas
    public static Command getAddShardHostToOtherShardsCommand(String otherShardsHosts) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                "-e",
                "\"" + otherShardsHosts + "\"",
                //add [echo -e "\nIP HOST\nIP HOST\nIP HOST"] with each replica's data
                //except the one to whom this command is aimed and replace placeholder
                ">>",
                "/etc/hosts"
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute on each replica
    public static Command getCheckReplicaSetMasterCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                "'db.isMaster()'",
                "|",
                "mongo"//primary replica's output will contain ["ismaster" : true]
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute on any one replica
    public static Command getFindReplicaSetMasterCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
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
        return cmd;
    }

    //execute on primary replica
    public static Command getAddSecondaryReplicasToPrimaryCommand(String secondaryNodes) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongod");
        req.setArgs(Arrays.asList(
                "--config",
                "/etc/mongodb.conf",
                "&&",
                "/bin/echo",
                "-e",
                String.format("'rs.initiate()'%s", secondaryNodes),
                //add each secondary node newline-separated and replace placeholder
                //e.g.: [\n'rs.add(\":NON_PRIMARY_REPLICA_HOST\")']
                "|",
                "mongo"
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute on primary replica
    //used for adding shard to existing cluster
    public static Command getAddSecondaryReplicasToPrimaryExistCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongod");
        req.setArgs(Arrays.asList(
                "--config",
                "/etc/mongodb.conf",
                "&&",
                "/bin/echo",
                "-e",
                ":SECONDARY_REPLICAS",
                //add each secondary node newline-separated and replace placeholder
                //e.g.: [\n'rs.add(\":NON_PRIMARY_REPLICA_HOST\")']
                "|",
                "mongo"
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute on any cluster member
    public static Command getRegisterPrimaryOnRouterCommand(String replicaSetName, String primaryHost, String routerHost) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                String.format("'sh.addShard(\"%s/%s:%s\")'",
                        replicaSetName, primaryHost, Constants.MONGO_SHARD_PORT),
                "|",
                "mongo",
                "--host",
                routerHost, //supply any one router host
                "--port",
                Constants.MONGO_ROUTER_PORT + "" //supply router port
        ));
        req.setTimeout(60);
        return cmd;
    }

    //execute on any router member
    public static Command getRegisterPrimaryWithRouterCommand(String replicaSetName, String primaryHost) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/echo");
        req.setArgs(Arrays.asList(
                String.format("'sh.addShard(\"%s/%s:%s\")'",
                        replicaSetName, primaryHost, Constants.MONGO_SHARD_PORT),
                "|",
                "mongo"
        ));
        req.setTimeout(60);
        return cmd;
    }

    // LIFECYCLE COMMANDS =======================================================
    //execute on config server
    public static Command getStartConfigServerCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/mkdir");
        req.setArgs(Arrays.asList(
                "-p",
                "/data/configdb",
                ";",
                "daemon",//?????
                "mongod",
                "--configsvr",
                "--dbpath",
                "/data/configdb",
                "--port",
                Constants.MONGO_CONFIG_SERVER_PORT + "", // this might be user-supplied
                "--fork",
                "--logpath",
                "/var/log/mongodb.log",
                "&"
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on router
    public static Command getStartRouterCommand(String configServersArg) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongos");
        req.setArgs(Arrays.asList(
                "--configdb", configServersArg
        //add config servers (with ports) based on user selection, comma-separated
        //e.g.: cfg0.example.net:27019,cfg1.example.net:27019,cfg2.example.net:27019
        //and replace placeholder
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on shard
    public static Command getStartShardCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/service");
        req.setArgs(Arrays.asList(
                "mongodb",
                "start"
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on shard
    public static Command getStopShardCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/service");
        req.setArgs(Arrays.asList(
                "mongodb",
                "stop"
        ));
        req.setTimeout(60);
        return cmd;
    }

    //execute on shard
    public static Command getRestartShardCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/service");
        req.setArgs(Arrays.asList(
                "mongodb",
                "restart"
        ));
        req.setTimeout(180);
        return cmd;
    }

    //execute on shard
    public static Command getShardStatusCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/service");
        req.setArgs(Arrays.asList(
                "mongodb",
                "status"//output shall contain [mongodb start/running] or [mongodb stop/waiting]
        ));
        req.setTimeout(10);
        return cmd;
    }

    //execute on any cluster member
    public static Command getCheckVersionCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--version"//output shall contain [MongoDB shell version]
        ));
        req.setTimeout(10);
        return cmd;
    }

    //execute on any cluster member
    public static Command getCheckInstanceRunningCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongo");
        req.setArgs(Arrays.asList(
                "--host",
                ":MONGO_HOST", //supply host of node under examination
                "--port",
                ":MONGO_PORT" //supply port of node under examination
        ));
        req.setTimeout(30);
        return cmd;
    }

    //execute on any cluster member
    public static Command getShutdownMongodCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("mongod");
        req.setArgs(Arrays.asList(
                "--shutdown"
        ));
        req.setTimeout(60);
        return cmd;
    }

    //execute on any cluster member
    public static Command getShutdownMongod2Command() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/kill");
        req.setArgs(Arrays.asList(
                "-2",
                "`pgrep -f mongod`"
        ));
        req.setTimeout(10);
        return cmd;
    }

    public static Command getForceKillMongodCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/bin/kill");
        req.setArgs(Arrays.asList(
                "-9",
                "`pgrep -f mongod`"
        ));
        req.setTimeout(30);
        return cmd;
    }

    // RECONFIGURATION COMMANDS
}
