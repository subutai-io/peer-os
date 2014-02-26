/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands;

import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class CassandraCommands {

//    private static final String conf = "/opt/cassandra-2.0.4/conf/cassandra.yaml";
    private static final String conf = "$CASSANDRA_HOME/conf/cassandra.yaml";
    private static final String etcProfile = ". /etc/profile && ";

    // INSTALLATION COMMANDS ===================================================
    public static Request getTemplate() {
        return CommandFactory.newRequest(
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
                180); //                        timeout (sec)
    }

//    public static Command getInstallCommand() {
//        Command cmd = getTemplate();
//        Request req = getTemplate();
//        req.setProgram("/usr/bin/apt-get");
//        req.setArgs(Arrays.asList(
//                "--force-yes",
//                "--assume-yes",
//                "install",
//                "ksks-cassandra"
//        ));
//        req.setTimeout(180);
//        return req;
//    }

//    public static Command getUninstallCommand() {
//        Command cmd = getTemplate();
//        Request req = getTemplate();
//        req.setProgram("/usr/bin/apt-get");
//        req.setArgs(Arrays.asList(
//                "--force-yes",
//                "--assume-yes",
//                "purge",
//                "ksks-cassandra"
//        ));
//        req.setTimeout(180);
//        return req;
//    }

    public static Request getSetListenAddressCommand(String listenAddress) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/listen_address:/=' " + conf + ")`'s!.*!listen_address: %listenAddress!'";
        req.setProgram(program.replace("%listenAddress", listenAddress));
        return req;
    }

    public static Request getSetRpcAddressCommand(String rpcAddress) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/rpc_address:/=' " + conf + ")`'s!.*!rpc_address: %rpcAddress!'";
        req.setProgram(program.replace("%rpcAddress", rpcAddress));
        return req;
    }

    public static Request getSetSeedsCommand(String seeds) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/- seeds:/=' " + conf + ")`'s!.*!             - seeds: %seedsIps!'";
        req.setProgram(program.replace("%seedsIps", '"' + seeds + '"'));
        return req;
    }

    public static Request getSetClusterNameCommand(String clusterName) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/cluster_name:/=' " + conf + ")`'s!.*!cluster_name: %clusterName!'";
        req.setProgram(program.replace("%clusterName", clusterName));
        return req;
    }

    public static Request getSetDataDirectoryCommand(String dir) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/data_file_directories:/=' " + conf + ") + 1`'s!.*!     - %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return req;
    }

    public static Request getSetCommitLogDirectoryCommand(String dir) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/commitlog_directory:/=' " + conf + ")`'s!.*!commitlog_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return req;
    }

    public static Request getSetSavedCachesDirectoryCommand(String dir) {
        
        Request req = getTemplate();
        String program = etcProfile + "sed -i " + conf + " -e `expr $(sed -n '/saved_caches_directory:/=' " + conf + ")`'s!.*!saved_caches_directory: %dir!'";
        req.setProgram(program.replace("%dir", dir));
        return req;
    }

    public static Request getDeleteDataDirectoryCommand() {
        
        Request req = getTemplate();
        req.setProgram("rm -rf /var/lib/cassandra/data/*");
        return req;
    }

    public static Request getDeleteCommitLogDirectoryCommand() {
        
        Request req = getTemplate();
        req.setProgram("rm -rf /var/lib/cassandra/commitlog/*");
        return req;
    }

    public static Request getDeleteSavedCachesDirectoryCommand() {
        
        Request req = getTemplate();
        req.setProgram("rm -rf /var/lib/cassandra/saved_caches/*");
        return req;
    }

    public static Request getSourceEtcProfileUpdateCommand() {
        
        Request req = getTemplate();
        req.setProgram(". /etc/profile");
        return req;
    }

    public static Request getServiceCassandraStartCommand() {
        
        Request req = getTemplate();
        req.setProgram("service cassandra start");
        return req;
    }

    public static Request getServiceCassandraStopCommand() {
        
        Request req = getTemplate();
        req.setProgram("service cassandra stop");
        return req;
    }

    public static Request getServiceCassandraStatusCommand() {
        
        Request req = getTemplate();
        req.setProgram("service cassandra status");
        return req;
    }

    public static Request getAptGetUpdate() {
        
        Request req = getTemplate();
        req.setProgram("apt-get update");
        return req;
    }

    public Request getCommand(CassandraCommandEnum cce) {
        
        Request req = getTemplate();
        req.setProgram(cce.getProgram());
        return req;
    }

}
