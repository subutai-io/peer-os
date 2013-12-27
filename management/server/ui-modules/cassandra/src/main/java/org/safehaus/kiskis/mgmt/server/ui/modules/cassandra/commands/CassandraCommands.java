/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands;

import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
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

    private static final String conf = "/opt/cassandra-2.0.0/conf/cassandra.yaml";

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

    public static Command getSetListenAddressCommand(String listenAddress) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/listen_address:/=' " + conf + ")`'s!.*!listen_address: %listenAddress!'";
        req.setProgram(program.replace("%listenAddress", listenAddress));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetRpcAddressCommand(String rpcAddress) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/rpc_address:/=' " + conf + ")`'s!.*!rpc_address: %rpcAddress!'";
        req.setProgram(program.replace("%rpcAddress", rpcAddress));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetSeedsCommand(String seeds) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/- seeds:/=' " + conf + ")`'s!.*!             - seeds: %seedsIps!'";
        req.setProgram(program.replace("%seedsIps", '"' + seeds + '"'));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetClusterNameCommand(String clusterName) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/cluster_name:/=' " + conf + ")`'s!.*!cluster_name: %clusterName!'";
        req.setProgram(program.replace("%clusterName", clusterName));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetDataDirectoryCommand(String dir) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/data_file_directories:/=' " + conf + ") + 1`'s!.*!     - %dir!'";
        req.setProgram(program.replace("%dir", dir));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetCommitLogDirectoryCommand(String dir) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/commitlog_directory:/=' " + conf + ")`'s!.*!commitlog_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        req.setTimeout(30);
        return cmd;
    }

    public static Command getSetSavedCachesDirectoryCommand(String dir) {
        Command cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/saved_caches_directory:/=' " + conf + ")`'s!.*!saved_caches_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        req.setTimeout(30);
        return cmd;
    }

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
        req.setProgram(". /etc/profile");
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
