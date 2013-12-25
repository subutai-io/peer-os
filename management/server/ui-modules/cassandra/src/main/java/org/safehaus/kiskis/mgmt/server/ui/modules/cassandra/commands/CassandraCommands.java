/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands;

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

}
