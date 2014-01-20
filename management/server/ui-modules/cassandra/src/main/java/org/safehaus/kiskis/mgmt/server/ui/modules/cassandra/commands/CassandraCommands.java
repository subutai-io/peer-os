/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands;

import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class CassandraCommands {

    private static final String conf = "/opt/cassandra-2.0.3/conf/cassandra.yaml";

    // INSTALLATION COMMANDS ===================================================
    public static CommandImpl getTemplate() {
        return (CommandImpl) CommandFactory.createRequest(
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
                60); //                        timeout (sec)
    }

//    public static Command getInstallCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/apt-get");
//        req.setArgs(Arrays.asList(
//                "--force-yes",
//                "--assume-yes",
//                "install",
//                "ksks-cassandra"
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }

//    public static Command getUninstallCommand() {
//        Command cmd = getTemplate();
//        Request req = cmd.getRequest();
//        req.setProgram("/usr/bin/apt-get");
//        req.setArgs(Arrays.asList(
//                "--force-yes",
//                "--assume-yes",
//                "purge",
//                "ksks-cassandra"
//        ));
//        req.setTimeout(180);
//        return cmd;
//    }

    public static CommandImpl getSetListenAddressCommand(String listenAddress) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/listen_address:/=' " + conf + ")`'s!.*!listen_address: %listenAddress!'";
        req.setProgram(program.replace("%listenAddress", listenAddress));
        return cmd;
    }

    public static CommandImpl getSetRpcAddressCommand(String rpcAddress) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/rpc_address:/=' " + conf + ")`'s!.*!rpc_address: %rpcAddress!'";
        req.setProgram(program.replace("%rpcAddress", rpcAddress));
        return cmd;
    }

    public static CommandImpl getSetSeedsCommand(String seeds) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/- seeds:/=' " + conf + ")`'s!.*!             - seeds: %seedsIps!'";
        req.setProgram(program.replace("%seedsIps", '"' + seeds + '"'));
        return cmd;
    }

    public static CommandImpl getSetClusterNameCommand(String clusterName) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/cluster_name:/=' " + conf + ")`'s!.*!cluster_name: %clusterName!'";
        req.setProgram(program.replace("%clusterName", clusterName));
        return cmd;
    }

    public static CommandImpl getSetDataDirectoryCommand(String dir) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/data_file_directories:/=' " + conf + ") + 1`'s!.*!     - %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return cmd;
    }

    public static CommandImpl getSetCommitLogDirectoryCommand(String dir) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/commitlog_directory:/=' " + conf + ")`'s!.*!commitlog_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return cmd;
    }

    public static CommandImpl getSetSavedCachesDirectoryCommand(String dir) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        String program = "sed -i " + conf + " -e `expr $(sed -n '/saved_caches_directory:/=' " + conf + ")`'s!.*!saved_caches_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return cmd;
    }

    public static CommandImpl getDeleteDataDirectoryCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/data/*");
        return cmd;
    }

    public static CommandImpl getDeleteCommitLogDirectoryCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/commitlog/*");
        return cmd;
    }

    public static CommandImpl getDeleteSavedCachesDirectoryCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("rm -rf /var/lib/cassandra/saved_caches/*");
        return cmd;
    }

    public static CommandImpl getSourceEtcProfileUpdateCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram(". /etc/profile");
        return cmd;
    }

    public static CommandImpl getServiceCassandraStartCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra start");
        return cmd;
    }

    public static CommandImpl getServiceCassandraStopCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra stop");
        return cmd;
    }

    public static CommandImpl getServiceCassandraStatusCommand() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("service cassandra status");
        return cmd;
    }

    public static CommandImpl getAptGetUpdate() {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram("apt-get update");
        return cmd;
    }

    public CommandImpl getCommand(CassandraCommandEnum cce) {
        CommandImpl cmd = getTemplate();
        Request req = cmd.getRequest();
        req.setProgram(cce.getProgram());
        return cmd;
    }

}
