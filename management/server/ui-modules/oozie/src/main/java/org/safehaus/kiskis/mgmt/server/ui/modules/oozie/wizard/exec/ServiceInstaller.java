package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec;

import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;

/**
 *
 * @author bahadyr
 */
public class ServiceInstaller {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    OozieConfig config;

    public ServiceInstaller(OozieConfig config, TextArea terminal) {
        this.terminal = terminal;
        this.config = config;
        OozieCommands oc = new OozieCommands();

        Task updateApt = RequestUtil.createTask("apt-get update");
        for (Agent agent : config.getServers()) {
            Command command = oc.getAptGetUpdate();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(updateApt.getUuid());
            command.getRequest().setRequestSequenceNumber(updateApt.getIncrementedReqSeqNumber());
            updateApt.addCommand(command);
        }
        tasks.add(updateApt);

        Task updateAptClients = RequestUtil.createTask("apt-get update");
        for (Agent agent : config.getClients()) {
            Command command = oc.getAptGetUpdate();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(updateAptClients.getUuid());
            command.getRequest().setRequestSequenceNumber(updateAptClients.getIncrementedReqSeqNumber());
            updateAptClients.addCommand(command);
        }
        tasks.add(updateAptClients);

        Task installServer = RequestUtil.createTask("Install Oozie Server");
        for (Agent agent : config.getServers()) {
            Command command = oc.getCommand(OozieCommandEnum.INSTALL_SERVER);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installServer.getUuid());
            command.getRequest().setRequestSequenceNumber(installServer.getIncrementedReqSeqNumber());
            installServer.addCommand(command);
        }
        tasks.add(installServer);

        Task installClient = RequestUtil.createTask("Install Oozie Client");
        for (Agent agent : config.getClients()) {
            Command command = oc.getCommand(OozieCommandEnum.INSTALL_CLIENT);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installClient.getUuid());
            command.getRequest().setRequestSequenceNumber(installClient.getIncrementedReqSeqNumber());
            installClient.addCommand(command);
        }
        tasks.add(installClient);

        Task configugeServer = RequestUtil.createTask("Configure server");
        for (Agent agent : config.getServers()) {
            Command command = oc.getSetConfigCommand("test");
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(configugeServer.getUuid());
            command.getRequest().setRequestSequenceNumber(configugeServer.getIncrementedReqSeqNumber());
            configugeServer.addCommand(command);
        }
        tasks.add(configugeServer);

        Task configugeClients = RequestUtil.createTask("Configure client");
        for (Agent agent : config.getServers()) {
            Command command = oc.getSetConfigCommand("test");
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(configugeClients.getUuid());
            command.getRequest().setRequestSequenceNumber(configugeClients.getIncrementedReqSeqNumber());
            configugeClients.addCommand(command);
        }
        tasks.add(configugeClients);

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

    private void saveHBaseInfo() {
        if (OozieDAO.saveClusterInfo(config)) {
            terminal.setValue(terminal.getValue().toString() + config.getUuid() + " cluster saved into keyspace.\n");
        }
    }

    private void executeCommand(Command command) {
        terminal.setValue(terminal.getValue().toString() + command.getRequest().getProgram() + "\n");
        ServiceLocator.getService(CommandManager.class).executeCommand(command);
    }

}
