/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import com.vaadin.ui.TextArea;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class Installer {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    CassandraConfig config;

    public Installer(CassandraConfig config, TextArea terminal) {
        this.terminal = terminal;
        this.config = config;

        Task installTask = RequestUtil.createTask(HBaseModule.getCommandManager(), "Install Cassandra");
        for (Agent agent : config.getSelectedAgents()) {
            Command command = CassandraCommands.getInstallCommand();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installTask.getUuid());
            command.getRequest().setRequestSequenceNumber(installTask.getIncrementedReqSeqNumber());
            installTask.addCommand(command);
        }
        tasks.add(installTask);

        Task setListenAddressTask = RequestUtil.createTask(HBaseModule.getCommandManager(), "Set listen addresses");
        for (Agent agent : config.getSelectedAgents()) {
            Command sourceEtcProfileCommand = CassandraCommands.getSourceEtcProfileUpdateCommand();
            sourceEtcProfileCommand.getRequest().setUuid(agent.getUuid());
            sourceEtcProfileCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            sourceEtcProfileCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(sourceEtcProfileCommand);

            Command setListenAddressCommand = CassandraCommands.getSetListenAddressCommand(agent.getHostname() + "." + config.getDomainName());
//            Command setListenAddressCommand = CassandraCommands.getSetListenAddressCommand(agent.getListIP().get(0));
            setListenAddressCommand.getRequest().setUuid(agent.getUuid());
            setListenAddressCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            setListenAddressCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(setListenAddressCommand);

            Command setRpcAddressCommand = CassandraCommands.getSetRpcAddressCommand(agent.getHostname() + "." + config.getDomainName());
//            Command setRpcAddressCommand = CassandraCommands.getSetRpcAddressCommand(agent.getListIP().get(0));
            setRpcAddressCommand.getRequest().setUuid(agent.getUuid());
            setRpcAddressCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            setRpcAddressCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(setRpcAddressCommand);

            StringBuilder seedsSB = new StringBuilder();
            for (Agent seed : config.getSeeds()) {
                seedsSB.append(seed.getHostname()).append(".").append(config.getDomainName()).append(",");
//                seedsSB.append(agent.getListIP().get(0)).append(",");
            }

            Command setSeedsCommand = CassandraCommands.getSetSeedsCommand(seedsSB.substring(0, seedsSB.length() - 1));
            setSeedsCommand.getRequest().setUuid(agent.getUuid());
            setSeedsCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            setSeedsCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(setSeedsCommand);
        }
        tasks.add(setListenAddressTask);

        if (!config.getClusterName().isEmpty()) {
            Task clusterRenameTask = RequestUtil.createTask(HBaseModule.getCommandManager(), "Rename cluster");
            for (Agent agent : config.getSelectedAgents()) {
                Command setClusterNameCommand = CassandraCommands.getSetClusterNameCommand(config.getClusterName());
                setClusterNameCommand.getRequest().setUuid(agent.getUuid());
                setClusterNameCommand.getRequest().setTaskUuid(clusterRenameTask.getUuid());
                setClusterNameCommand.getRequest().setRequestSequenceNumber(clusterRenameTask.getIncrementedReqSeqNumber());
                clusterRenameTask.addCommand(setClusterNameCommand);

                Command deleteDataDir = CassandraCommands.getDeleteDataDirectoryCommand();
                deleteDataDir.getRequest().setUuid(agent.getUuid());
                deleteDataDir.getRequest().setTaskUuid(clusterRenameTask.getUuid());
                deleteDataDir.getRequest().setRequestSequenceNumber(clusterRenameTask.getIncrementedReqSeqNumber());
                clusterRenameTask.addCommand(deleteDataDir);

                Command deleteCommitLogDit = CassandraCommands.getDeleteCommitLogDirectoryCommand();
                deleteCommitLogDit.getRequest().setUuid(agent.getUuid());
                deleteCommitLogDit.getRequest().setTaskUuid(clusterRenameTask.getUuid());
                deleteCommitLogDit.getRequest().setRequestSequenceNumber(clusterRenameTask.getIncrementedReqSeqNumber());
                clusterRenameTask.addCommand(deleteCommitLogDit);

                Command deleteSavedCashesDir = CassandraCommands.getDeleteSavedCachesDirectoryCommand();
                deleteSavedCashesDir.getRequest().setUuid(agent.getUuid());
                deleteSavedCashesDir.getRequest().setTaskUuid(clusterRenameTask.getUuid());
                deleteSavedCashesDir.getRequest().setRequestSequenceNumber(clusterRenameTask.getIncrementedReqSeqNumber());
                clusterRenameTask.addCommand(deleteSavedCashesDir);

            }
            tasks.add(clusterRenameTask);
        }

        if (!config.getDataDirectory().isEmpty()) {
            Task setDataDirectory = RequestUtil.createTask(HBaseModule.getCommandManager(), "Change data directory");
            for (Agent agent : config.getSelectedAgents()) {
                Command setDataDir = CassandraCommands.getSetDataDirectoryCommand(config.getDataDirectory());
                setDataDir.getRequest().setUuid(agent.getUuid());
                setDataDir.getRequest().setTaskUuid(setDataDirectory.getUuid());
                setDataDir.getRequest().setRequestSequenceNumber(setDataDirectory.getIncrementedReqSeqNumber());
                setDataDirectory.addCommand(setDataDir);

            }
            tasks.add(setDataDirectory);
        }

        if (!config.getDataDirectory().isEmpty()) {
            Task setCommitLogDirectoryTask = RequestUtil.createTask(HBaseModule.getCommandManager(), "Change Commit log directory");
            for (Agent agent : config.getSelectedAgents()) {
                Command setCommitLogDir = CassandraCommands.getSetCommitLogDirectoryCommand(config.getCommitLogDirectory());
                setCommitLogDir.getRequest().setUuid(agent.getUuid());
                setCommitLogDir.getRequest().setTaskUuid(setCommitLogDirectoryTask.getUuid());
                setCommitLogDir.getRequest().setRequestSequenceNumber(setCommitLogDirectoryTask.getIncrementedReqSeqNumber());
                setCommitLogDirectoryTask.addCommand(setCommitLogDir);
            }
            tasks.add(setCommitLogDirectoryTask);
        }

        if (!config.getDataDirectory().isEmpty()) {
            Task setSavedCashesDirectoryTask = RequestUtil.createTask(HBaseModule.getCommandManager(), "Change Saved caches directory");
            for (Agent agent : config.getSelectedAgents()) {
                Command setSavedCachesDir = CassandraCommands.getSetSavedCachesDirectoryCommand(config.getSavedCachesDirectory());
                setSavedCachesDir.getRequest().setUuid(agent.getUuid());
                setSavedCachesDir.getRequest().setTaskUuid(setSavedCashesDirectoryTask.getUuid());
                setSavedCachesDir.getRequest().setRequestSequenceNumber(setSavedCashesDirectoryTask.getIncrementedReqSeqNumber());
                setSavedCashesDirectoryTask.addCommand(setSavedCachesDir);

            }
            tasks.add(setSavedCashesDirectoryTask);
        }
    }

    public void start() {
        currentTask = tasks.poll();
        if (currentTask != null) {
            for (Command command : currentTask.getCommands()) {
                executeCommand(command);
            }
        }
    }

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public void onResponse(Response response) {
        if (currentTask != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {

            Task task = HBaseModule.getCommandManager().getTask(response.getTaskUuid());
            List<ParseResult> list = HBaseModule.getCommandManager().parseTask(task, true);
            task = HBaseModule.getCommandManager().getTask(response.getTaskUuid());
            if (!list.isEmpty() && terminal != null) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    terminal.setValue(terminal.getValue().toString() + "\n" + task.getDescription() + " successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step " + task.getDescription());
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nInstallation finished");
                        CassandraClusterInfo cci = new CassandraClusterInfo();
                        cci.setName(config.getClusterName());
                        cci.setDataDir(config.getDataDirectory());
                        cci.setCommitLogDir(config.getCommitLogDirectory());
                        cci.setSavedCacheDir(config.getSavedCachesDirectory());
                        cci.setSeeds(config.getSeedsUUIDList());
                        cci.setNodes(config.getAgentsUUIDList());
                        cci.setDomainName(config.getDomainName());

                        HBaseModule.getCommandManager().saveCassandraClusterData(cci);
                    }
                } else if (task.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                    terminal.setValue("\n" + task.getDescription() + " failed");
                }
            }

        }
    }

    private void executeCommand(Command command) {
        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        HBaseModule.getCommandManager().executeCommand(command);
    }

}
