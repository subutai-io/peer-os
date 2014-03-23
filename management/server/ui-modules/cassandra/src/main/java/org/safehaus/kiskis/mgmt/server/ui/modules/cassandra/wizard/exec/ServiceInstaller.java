/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao.CassandraClusterInfo;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import java.util.LinkedList;
import java.util.Queue;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao.CassandraDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraCommandEnum;

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

        Task updateApt = new Task("apt-get update");
        for (Agent agent : config.getSelectedAgents()) {
            Request command = CassandraCommands.getAptGetUpdate();
            command.setUuid(agent.getUuid());
            updateApt.addRequest(command);
        }
        tasks.add(updateApt);

        Task installTask = new Task("Install Cassandra");
        for (Agent agent : config.getSelectedAgents()) {
            Request command = new CassandraCommands().getCommand(CassandraCommandEnum.INSTALL);
            command.setUuid(agent.getUuid());
            installTask.addRequest(command);
        }
        tasks.add(installTask);

        Task sourceEtcProfileTask = new Task("Update profile");
        for (Agent agent : config.getSelectedAgents()) {
            Request sourceEtcProfileCommand = CassandraCommands.getSourceEtcProfileUpdateCommand();
            sourceEtcProfileCommand.setUuid(agent.getUuid());
            sourceEtcProfileTask.addRequest(sourceEtcProfileCommand);
        }
        tasks.add(sourceEtcProfileTask);

        Task setListenAddressTask = new Task("Set listen addresses");
        for (Agent agent : config.getSelectedAgents()) {
            Request setListenAddressCommand = CassandraCommands.getSetListenAddressCommand(agent.getHostname() + "." + config.getDomainName());
            setListenAddressCommand.setUuid(agent.getUuid());
            setListenAddressTask.addRequest(setListenAddressCommand);

            Request setRpcAddressCommand = CassandraCommands.getSetRpcAddressCommand(agent.getHostname() + "." + config.getDomainName());
            setRpcAddressCommand.setUuid(agent.getUuid());
            setListenAddressTask.addRequest(setRpcAddressCommand);

        }
        tasks.add(setListenAddressTask);

        Task setSeedsTask = new Task("Set seeds addresses");
        StringBuilder seedsSB = new StringBuilder();
        for (Agent agent : config.getSeeds()) {
            seedsSB.append(agent.getHostname()).append(".").append(config.getDomainName()).append(",");
        }
        for (Agent agent : config.getSelectedAgents()) {
            Request setSeedsCommand = CassandraCommands.getSetSeedsCommand(seedsSB.substring(0, seedsSB.length() - 1));
            setSeedsCommand.setUuid(agent.getUuid());
            setSeedsTask.addRequest(setSeedsCommand);
        }
        tasks.add(setSeedsTask);

        if (!config.getClusterName().isEmpty()) {
            Task clusterRenameTask = new Task("Rename cluster");
            for (Agent agent : config.getSelectedAgents()) {
                Request setClusterNameCommand = CassandraCommands.getSetClusterNameCommand(config.getClusterName());
                setClusterNameCommand.setUuid(agent.getUuid());
                clusterRenameTask.addRequest(setClusterNameCommand);

                Request deleteDataDir = CassandraCommands.getDeleteDataDirectoryCommand();
                deleteDataDir.setUuid(agent.getUuid());
                clusterRenameTask.addRequest(deleteDataDir);

                Request deleteCommitLogDit = CassandraCommands.getDeleteCommitLogDirectoryCommand();
                deleteCommitLogDit.setUuid(agent.getUuid());
                clusterRenameTask.addRequest(deleteCommitLogDit);

                Request deleteSavedCashesDir = CassandraCommands.getDeleteSavedCachesDirectoryCommand();
                deleteSavedCashesDir.setUuid(agent.getUuid());
                clusterRenameTask.addRequest(deleteSavedCashesDir);

            }
            tasks.add(clusterRenameTask);
        }

        if (!config.getDataDirectory().isEmpty()) {
            Task setDataDirectory = new Task("Change data directory");
            for (Agent agent : config.getSelectedAgents()) {
                Request setDataDir = CassandraCommands.getSetDataDirectoryCommand(config.getDataDirectory());
                setDataDir.setUuid(agent.getUuid());
                setDataDirectory.addRequest(setDataDir);

            }
            tasks.add(setDataDirectory);
        }

        if (!config.getCommitLogDirectory().isEmpty()) {
            Task setCommitLogDirectoryTask = new Task("Change Commit log directory");
            for (Agent agent : config.getSelectedAgents()) {
                Request setCommitLogDir = CassandraCommands.getSetCommitLogDirectoryCommand(config.getCommitLogDirectory());
                setCommitLogDir.setUuid(agent.getUuid());
                setCommitLogDirectoryTask.addRequest(setCommitLogDir);
            }
            tasks.add(setCommitLogDirectoryTask);
        }

        if (!config.getSavedCachesDirectory().isEmpty()) {
            Task setSavedCashesDirectoryTask = new Task("Change Saved caches directory");
            for (Agent agent : config.getSelectedAgents()) {
                Request setSavedCachesDir = CassandraCommands.getSetSavedCachesDirectoryCommand(config.getSavedCachesDirectory());
                setSavedCachesDir.setUuid(agent.getUuid());
                setSavedCashesDirectoryTask.addRequest(setSavedCachesDir);

            }
            tasks.add(setSavedCashesDirectoryTask);
        }
    }

    public void start() {
        terminal.setValue("Cassandra cluster installation started...\n");
        moveToNextTask();
        if (currentTask != null) {
            terminal.setValue(terminal.getValue() + currentTask.peekNextRequest().getProgram() + "\n");

            CassandraModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.\n");
                        moveToNextTask();
                        if (currentTask != null) {

                            terminal.setValue(terminal.getValue().toString() + "Running next step " + currentTask.getDescription() + "\n");
                            terminal.setValue(terminal.getValue() + currentTask.peekNextRequest().getProgram() + "\n");
//                            for (Request command : currentTask.getCommands()) {
//                                executeCommand(command);
//                            }
                            return currentTask;
                        } else {
                            terminal.setValue(terminal.getValue().toString() + "Tasks complete.\n");
                            saveCCI();
                        }
                    } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                        terminal.setValue(terminal.getValue().toString() + task.getDescription() + " failed\n");
                    }
                    terminal.setCursorPosition(terminal.getValue().toString().length());
                    return null;
                }
            });
        }
    }

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

    private void saveCCI() {
        CassandraClusterInfo cci = new CassandraClusterInfo();
        cci.setName(config.getClusterName());
        cci.setDataDir(config.getDataDirectory());
        cci.setCommitLogDir(config.getCommitLogDirectory());
        cci.setSavedCacheDir(config.getSavedCachesDirectory());
        cci.setSeeds(config.getSeedsUUIDList());
        cci.setNodes(config.getAgentsUUIDList());
        cci.setDomainName(config.getDomainName());

        if (CassandraDAO.saveCassandraClusterInfo(cci)) {
            terminal.setValue(terminal.getValue().toString() + cci.getUuid() + " cluster saved into keyspace.\n");
        }
    }

//    private void executeCommand(Request command) {
//        terminal.setValue(terminal.getValue() + command.getProgram() + "\n");
//        ServiceLocator.getService(CommandManager.class).executeCommand(command);
//    }
}
