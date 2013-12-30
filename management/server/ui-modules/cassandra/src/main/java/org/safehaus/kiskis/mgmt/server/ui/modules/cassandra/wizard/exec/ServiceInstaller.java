/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author bahadyr
 */
public class ServiceInstaller {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    CassandraConfig config;

    public ServiceInstaller(CassandraConfig config, TextArea terminal) {
        this.terminal = terminal;
        this.config = config;

        Task updateApt = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "apt-get update");
        for (Agent agent : config.getSelectedAgents()) {
            Command command = CassandraCommands.getAptGetUpdate();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(updateApt.getUuid());
            command.getRequest().setRequestSequenceNumber(updateApt.getIncrementedReqSeqNumber());
            updateApt.addCommand(command);
        }
        tasks.add(updateApt);
        
        Task installTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Install Cassandra");
        for (Agent agent : config.getSelectedAgents()) {
            Command command = CassandraCommands.getInstallCommand();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installTask.getUuid());
            command.getRequest().setRequestSequenceNumber(installTask.getIncrementedReqSeqNumber());
            installTask.addCommand(command);
        }
        tasks.add(installTask);

        Task sourceEtcProfileTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Update profile");
        for (Agent agent : config.getSelectedAgents()) {
            Command sourceEtcProfileCommand = CassandraCommands.getSourceEtcProfileUpdateCommand();
            sourceEtcProfileCommand.getRequest().setUuid(agent.getUuid());
            sourceEtcProfileCommand.getRequest().setTaskUuid(sourceEtcProfileTask.getUuid());
            sourceEtcProfileCommand.getRequest().setRequestSequenceNumber(sourceEtcProfileTask.getIncrementedReqSeqNumber());
            sourceEtcProfileTask.addCommand(sourceEtcProfileCommand);
        }
        tasks.add(sourceEtcProfileTask);

        Task setListenAddressTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Set listen addresses");
        for (Agent agent : config.getSelectedAgents()) {
//            Command setListenAddressCommand = CassandraCommands.getSetListenAddressCommand(agent.getHostname() + "." + config.getDomainName());
            Command setListenAddressCommand = CassandraCommands.getSetListenAddressCommand(agent.getListIP().get(0));
            setListenAddressCommand.getRequest().setUuid(agent.getUuid());
            setListenAddressCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            setListenAddressCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(setListenAddressCommand);

//            Command setRpcAddressCommand = CassandraCommands.getSetRpcAddressCommand(agent.getHostname() + "." + config.getDomainName());
            Command setRpcAddressCommand = CassandraCommands.getSetRpcAddressCommand(agent.getListIP().get(0));
            setRpcAddressCommand.getRequest().setUuid(agent.getUuid());
            setRpcAddressCommand.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            setRpcAddressCommand.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(setRpcAddressCommand);

        }
        tasks.add(setListenAddressTask);

        Task setSeedsTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Set seeds addresses");
        StringBuilder seedsSB = new StringBuilder();
        for (Agent agent : config.getSeeds()) {
//            seedsSB.append(seed.getHostname()).append(".").append(config.getDomainName()).append(",");
            seedsSB.append(agent.getListIP().get(0)).append(",");
            
        }
        for (Agent agent : config.getSelectedAgents()) {
            Command setSeedsCommand = CassandraCommands.getSetSeedsCommand(seedsSB.substring(0, seedsSB.length() - 1));
            setSeedsCommand.getRequest().setUuid(agent.getUuid());
            setSeedsCommand.getRequest().setTaskUuid(setSeedsTask.getUuid());
            setSeedsCommand.getRequest().setRequestSequenceNumber(setSeedsTask.getIncrementedReqSeqNumber());
            setSeedsTask.addCommand(setSeedsCommand);
        }
        tasks.add(setSeedsTask);

        if (!config.getClusterName().isEmpty()) {
            Task clusterRenameTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Rename cluster");
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
            Task setDataDirectory = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Change data directory");
            for (Agent agent : config.getSelectedAgents()) {
                Command setDataDir = CassandraCommands.getSetDataDirectoryCommand(config.getDataDirectory());
                setDataDir.getRequest().setUuid(agent.getUuid());
                setDataDir.getRequest().setTaskUuid(setDataDirectory.getUuid());
                setDataDir.getRequest().setRequestSequenceNumber(setDataDirectory.getIncrementedReqSeqNumber());
                setDataDirectory.addCommand(setDataDir);

            }
            tasks.add(setDataDirectory);
        }

        if (!config.getCommitLogDirectory().isEmpty()) {
            Task setCommitLogDirectoryTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Change Commit log directory");
            for (Agent agent : config.getSelectedAgents()) {
                Command setCommitLogDir = CassandraCommands.getSetCommitLogDirectoryCommand(config.getCommitLogDirectory());
                setCommitLogDir.getRequest().setUuid(agent.getUuid());
                setCommitLogDir.getRequest().setTaskUuid(setCommitLogDirectoryTask.getUuid());
                setCommitLogDir.getRequest().setRequestSequenceNumber(setCommitLogDirectoryTask.getIncrementedReqSeqNumber());
                setCommitLogDirectoryTask.addCommand(setCommitLogDir);
            }
            tasks.add(setCommitLogDirectoryTask);
        }

        if (!config.getSavedCachesDirectory().isEmpty()) {
            Task setSavedCashesDirectoryTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Change Saved caches directory");
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
        terminal.setValue("Cassandra cluster installation started...");
        moveToNextTask();
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

            List<ParseResult> list = ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true);
            Task task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            if (!list.isEmpty() && terminal != null) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step " + currentTask.getDescription());
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nTasks complete.\n");

                        CassandraClusterInfo cci = new CassandraClusterInfo();
                        cci.setName(config.getClusterName());
                        cci.setDataDir(config.getDataDirectory());
                        cci.setCommitLogDir(config.getCommitLogDirectory());
                        cci.setSavedCacheDir(config.getSavedCachesDirectory());
                        cci.setSeeds(config.getSeedsUUIDList());
                        cci.setNodes(config.getAgentsUUIDList());
                        cci.setDomainName(config.getDomainName());

                        if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                            terminal.setValue(terminal.getValue().toString() + "\n" + cci.getUuid() + " cluster saved into keyspace.");
                        }
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue("\n" + task.getDescription() + " failed");
                }
            }
            terminal.setCursorPosition(terminal.getValue().toString().length() - 1);

        }
    }

    private void executeCommand(Command command) {
//        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        ServiceLocator.getService(CommandManagerInterface.class).executeCommand(command);
    }

}
