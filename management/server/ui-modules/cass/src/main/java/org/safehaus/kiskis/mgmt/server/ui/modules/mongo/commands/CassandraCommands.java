/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class CassandraCommands {

    // INSTALLATION COMMANDS ===================================================
    public static Command getTemplate() {
        return (Command) CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                CassandraModule.MODULE_NAME, //     source
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

    public static Command getInstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "install",
                "ksks-cassandra"
        ));
        req.setTimeout(180);
        return cmd;
    }

    public static Command getUninstallCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("/usr/bin/apt-get");
        req.setArgs(Arrays.asList(
                "--force-yes",
                "--assume-yes",
                "purge",
                "ksks-cassandra"
        ));
        req.setTimeout(180);
        return cmd;
    }

    public static Command getSetListenAddressCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/listen_address:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!listen_address: %ip!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetRpcAddressCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/rpc_address:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!rpc_address: %ip!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetSeedsCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/- seeds:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!             - seeds: \"%ips\"!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetClusterNameCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/cluster_name:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!cluster_name: \"%newName\"!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetDataDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/data_file_directories:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml) + 1`'s!.*!     - %dir!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetCommitLogDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/commitlog_directory:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!commitlog_directory:%dir!'");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetSavedCachesDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("sed -i /opt/cassandra-2.0.0/conf/cassandra.yaml -e `expr $(sed -n '/saved_caches_directory:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)`'s!.*!saved_caches_directory:%dir!'");
        req.setTimeout(30);
        return cmd;
    }

     // INSTALLATION COMMANDS END ===================================================
     // CONFIGURATION COMMANDS ===================================================
    public static Command getDeleteDataDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/data/*");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getDeleteCommitLogDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/commitlog/*");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getDeleteSavedCachesDirectoryCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/saved_caches/*");
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSourceEtcProfileUpdateCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("source /etc/profile");
        req.setTimeout(30);
        return cmd;
    }
    
    public static Command getServiceCassandraStartCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra start");
        req.setTimeout(30);
        return cmd;
    }
    
    public static Command getServiceCassandraStopCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra stop");
        req.setTimeout(30);
        return cmd;
    }
    
    public static Command getServiceCassandraStatusCommand() {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra status");
        req.setTimeout(30);
        return cmd;
    }

//    //execute on primary replica
//    public static Command getAddSecondaryReplicasToPrimaryCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("mongod");
//        req.setArgs(Arrays.asList(
//                "--config",
//                "/etc/mongodb.conf",
//                "&&",
//                "/bin/echo",
//                "-e",
//                "'rs.initiate()':SECONDARY_REPLICAS",
//                //add each secondary node newline-separated and replace placeholder
//                //e.g.: [\n'rs.add(\":NON_PRIMARY_REPLICA_HOST\")']
//                "|",
//                "mongo"
//        ));
//        req.setTimeout(30);
//        return cmd;
//    }
//
//    //execute on any cluster member
//    public static Command getRegisterPrimaryOnRouterCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/bin/echo");
//        req.setArgs(Arrays.asList(
//                "'sh.addShard(\":REPLICA_SET_NAME/:PRIMARY_REPLICA_HOST::PORT\")'",
//                "|",
//                "mongo",
//                "--host",
//                ":ROUTER_HOST", //supply any one router host
//                "--port",
//                ":ROUTER_PORT" //supply router port
//        ));
//        req.setTimeout(60);
//        return cmd;
//    }
//
//    // LIFECYCLE COMMANDS =======================================================
//    //execute on config server
//    public static Command getStartConfigServerCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/bin/mkdir");
//        req.setArgs(Arrays.asList(
//                "/data/configdb",
//                ";",
//                "mongod",
//                "--configsvr",
//                "--dbpath",
//                "/data/configdb",
//                "--port",
//                ":CONFIG_SERVER_PORT" // this might be user-supplied
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }
//
//    //execute on router
//    public static Command getStartRouterCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("mongos");
//        req.setArgs(Arrays.asList(
//                "--configdb :REPLICAS"
//        //add config servers (with ports) based on user selection, comma-separated
//        //e.g.: cfg0.example.net:27019,cfg1.example.net:27019,cfg2.example.net:27019
//        //and replace placeholder
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }
//
//    //execute on shard
//    public static Command getStartShardCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/service");
//        req.setArgs(Arrays.asList(
//                "mongodb",
//                "start"
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }
//
//    //execute on shard
//    public static Command getStopShardCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/service");
//        req.setArgs(Arrays.asList(
//                "mongodb",
//                "stop"
//        ));
//        req.setTimeout(60);
//        return cmd;
//    }
//
//    //execute on shard
//    public static Command getRestartShardCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/service");
//        req.setArgs(Arrays.asList(
//                "mongodb",
//                "restart"
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }
//
//    //execute on shard
//    public static Command getShardStatusCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/service");
//        req.setArgs(Arrays.asList(
//                "mongodb",
//                "status"//output shall contain [mongodb start/running] or [mongodb stop/waiting]
//        ));
//        req.setTimeout(10);
//        return cmd;
//    }
//
//    //execute on any cluster member
//    public static Command getCheckVersionCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("mongo");
//        req.setArgs(Arrays.asList(
//                "--version"//output shall contain [MongoDB shell version]
//        ));
//        req.setTimeout(10);
//        return cmd;
//    }
//
//    //execute on any cluster member
//    public static Command getCheckInstanceRunningCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("mongo");
//        req.setArgs(Arrays.asList(
//                "--host",
//                ":MONGO_HOST", //supply host of node under examination
//                "--port",
//                ":MONGO_PORT" //supply port of node under examination
//        ));
//        req.setTimeout(30);
//        return cmd;
//    }
//
//    //execute on any cluster member
//    public static Command getShutdownMongodCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("mongod");
//        req.setArgs(Arrays.asList(
//                "--shutdown"
//        ));
//        req.setTimeout(30);
//        return cmd;
//    }
//
//    //execute on any cluster member
//    public static Command getShutdownMongod2Command() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/bin/kill");
//        req.setArgs(Arrays.asList(
//                " -2",
//                "`pgrep -f mongod`"
//        ));
//        req.setTimeout(10);
//        return cmd;
//    }

}
