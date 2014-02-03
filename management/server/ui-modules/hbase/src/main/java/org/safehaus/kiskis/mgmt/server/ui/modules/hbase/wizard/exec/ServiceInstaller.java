/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec;

import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.commands.HBaseCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;

/**
 *
 * @author bahadyr
 */
public class ServiceInstaller {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    Config config;

    public ServiceInstaller(Config config, TextArea terminal) {
        this.terminal = terminal;
        this.config = config;

        Task updateApt = RequestUtil.createTask("apt-get update");
        for (Agent agent : config.getAgents()) {
            Command command = HBaseCommands.getAptGetUpdate();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(updateApt.getUuid());
            command.getRequest().setRequestSequenceNumber(updateApt.getIncrementedReqSeqNumber());
            updateApt.addCommand(command);
        }
        tasks.add(updateApt);

        Task installTask = RequestUtil.createTask("Install HBase");
        for (Agent agent : config.getAgents()) {
            Command command = new HBaseCommands().getCommand(HBaseCommandEnum.INSTALL);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installTask.getUuid());
            command.getRequest().setRequestSequenceNumber(installTask.getIncrementedReqSeqNumber());
            installTask.addCommand(command);
        }
        tasks.add(installTask);

        StringBuilder masterSB = new StringBuilder();
        for (Agent agent : config.getMaster()) {
            masterSB.append(agent.getHostname()).append(" ").append(agent.getHostname());
            break;
        }
        Task setMasterTask = RequestUtil.createTask("Set master");
        for (Agent agent : config.getAgents()) {
            Command command = HBaseCommands.getSetMasterCommand(masterSB.toString());
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(setMasterTask.getUuid());
            command.getRequest().setRequestSequenceNumber(setMasterTask.getIncrementedReqSeqNumber());
            setMasterTask.addCommand(command);
        }
        tasks.add(setMasterTask);

        StringBuilder regionSB = new StringBuilder();
        for (Agent agent : config.getRegion()) {
            regionSB.append(agent.getHostname()).append(" ");
        }
        Task setRegionTask = RequestUtil.createTask("Set region");
        for (Agent agent : config.getAgents()) {
            Command command = HBaseCommands.getSetRegionCommand(regionSB.toString());
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(setRegionTask.getUuid());
            command.getRequest().setRequestSequenceNumber(setRegionTask.getIncrementedReqSeqNumber());
            setRegionTask.addCommand(command);
        }
        tasks.add(setRegionTask);

        StringBuilder quorumSB = new StringBuilder();
        for (Agent agent : config.getQuorum()) {
            quorumSB.append(agent.getHostname()).append(" ");
        }
        Task setQuorumTask = RequestUtil.createTask("Set quorum");
        for (Agent agent : config.getAgents()) {
            Command command = HBaseCommands.getSetQuorumCommand(quorumSB.toString());
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(setQuorumTask.getUuid());
            command.getRequest().setRequestSequenceNumber(setQuorumTask.getIncrementedReqSeqNumber());
            setQuorumTask.addCommand(command);
        }
        tasks.add(setQuorumTask);

        StringBuilder backupSB = new StringBuilder();
        for (Agent agent : config.getBackupMasters()) {
            backupSB.append(agent.getHostname()).append(" ");
        }
        Task setBackupMastersTask = RequestUtil.createTask("Set backup masters");
        for (Agent agent : config.getAgents()) {
            Command command = HBaseCommands.getSetBackupMastersCommand(backupSB.toString());
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(setBackupMastersTask.getUuid());
            command.getRequest().setRequestSequenceNumber(setBackupMastersTask.getIncrementedReqSeqNumber());
            setBackupMastersTask.addCommand(command);
        }
        tasks.add(setBackupMastersTask);

    }

    public void start() {
        terminal.setValue("Starting installation...\n");
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

            List<ParseResult> list = RequestUtil.parseTask(response.getTaskUuid(), true);
            Task task = RequestUtil.getTask(response.getTaskUuid());
            if (!list.isEmpty() && terminal != null) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.\n");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "Running next step " + currentTask.getDescription() + "\n");
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "Tasks complete.\n");
//                        saveInfo();
                        saveHBaseInfo();
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " failed\n");
                }
            }
            terminal.setCursorPosition(terminal.getValue().toString().length());

        }
    }

//    private void saveInfo() {
//        HBaseClusterInfo info = new HBaseClusterInfo();
//        info.setDomainName(config.getDomainInfo());
//        info.setMaster(config.getMasterUUIDset());
//        info.setRegion(config.getRegionSet());
//        info.setQuorum(config.getQuorumSet());
//        info.setBmasters(config.getBackupMastersSet());
//        info.setAllnodes(config.getAgentsSet());
//
//        if (HBaseDAO.saveHBaseClusterInfo(info)) {
//            terminal.setValue(terminal.getValue().toString() + info.getUuid() + " cluster saved into keyspace.\n");
//        }
//    }
    private void saveHBaseInfo() {
        if (HBaseDAO.saveClusterInfo(config)) {
            terminal.setValue(terminal.getValue().toString() + config.getUuid() + " cluster saved into keyspace.\n");
        }
    }

    private void executeCommand(Command command) {
        terminal.setValue(terminal.getValue().toString() + command.getRequest().getProgram() + "\n");
        ServiceLocator.getService(CommandManager.class).executeCommand(command);
    }

}
