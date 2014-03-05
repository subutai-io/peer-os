package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec;

import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.Queue;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;

/**
 *
 * @author bahadyr
 */
public class ServiceInstaller implements TaskCallback {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    OozieConfig config;

    public ServiceInstaller(OozieConfig config, TextArea terminal) {
        this.terminal = terminal;
        this.config = config;
        OozieCommands oc = new OozieCommands();

        Task updateApt = new Task("apt-get update");
        Request commandServer = oc.getAptGetUpdate();
        commandServer.setUuid(config.getServer().getUuid());

        updateApt.addRequest(commandServer);
        tasks.add(updateApt);

        Task updateAptClients = new Task("apt-get update");
        for (Agent agent : config.getClients()) {
            Request command = oc.getAptGetUpdate();
            command.setUuid(agent.getUuid());
            updateAptClients.addRequest(command);
        }
        tasks.add(updateAptClients);

        Task installServer = new Task("Install Oozie Server");
        Request commandServerInstall = oc.getCommand(OozieCommandEnum.INSTALL_SERVER);
        commandServerInstall.setUuid(config.getServer().getUuid());
        installServer.addRequest(commandServerInstall);
        tasks.add(installServer);

        Task installClient = new Task("Install Oozie Client");
        for (Agent agent : config.getClients()) {
            Request command = oc.getCommand(OozieCommandEnum.INSTALL_CLIENT);
            command.setUuid(agent.getUuid());
            installClient.addRequest(command);
        }
        tasks.add(installClient);

        Task configugeServer = new Task("Configure server");
        Request commandConfigServer = oc.getSetConfigCommand(config.getServer().getListIP().get(0) + " root");
        commandConfigServer.setUuid(config.getServer().getUuid());
        configugeServer.addRequest(commandConfigServer);
        tasks.add(configugeServer);

        Task configugeClients = new Task("Configure client");
        for (Agent agent : config.getClients()) {
            Request command = oc.getSetConfigCommand(config.getServer().getListIP().get(0) + " root");
            command.setUuid(agent.getUuid());
            configugeClients.addRequest(command);
        }
        tasks.add(configugeClients);

    }

    public void start() {
        terminal.setValue("Starting installation...\n");
        moveToNextTask();
        if (currentTask != null) {
//            for (Request command : currentTask.getCommands()) {
//                executeCommand(command);
//            }
            OozieModule.getTaskRunner().executeTask(currentTask, this);
        }
    }

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

//    public void onResponse(Response response) {
//        if (currentTask != null && response.getTaskUuid() != null
//                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {
//            List<ParseResult> list = RequestUtil.parseTask(response.getTaskUuid(), true);
//            Task task = RequestUtil.getTask(response.getTaskUuid());
//            if (!list.isEmpty() && terminal != null) {
//                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
//                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.\n");
//                    moveToNextTask();
//                    if (currentTask != null) {
//                        terminal.setValue(terminal.getValue().toString() + "Running next step " + currentTask.getDescription() + "\n");
//                        for (Request command : currentTask.getCommands()) {
//                            executeCommand(command);
//                        }
//                    } else {
//                        terminal.setValue(terminal.getValue().toString() + "Tasks complete.\n");
//                        saveHBaseInfo();
//                    }
//                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
//                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " failed\n");
//                }
//            }
//            terminal.setCursorPosition(terminal.getValue().toString().length());
//
//        }
//    }
    private void saveInfo() {
        if (OozieDAO.saveClusterInfo(config)) {
            terminal.setValue(terminal.getValue().toString() + config.getUuid() + " cluster saved into keyspace.\n");
        }
    }

//    private void executeCommand(Request command) {
//        terminal.setValue(terminal.getValue().toString() + command.getProgram() + "\n");
//        ServiceLocator.getService(CommandManager.class).executeCommand(command);
//    }
    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
        if (task.getTaskStatus() == TaskStatus.SUCCESS) {
            terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.\n");
            moveToNextTask();
            if (currentTask != null) {
                terminal.setValue(terminal.getValue().toString() + "Running next step " + currentTask.getDescription() + "\n");
                return currentTask;
            } else {
                terminal.setValue(terminal.getValue().toString() + "Tasks complete.\n");
                saveInfo();
            }
        } else if (task.getTaskStatus() == TaskStatus.FAIL) {
            terminal.setValue(terminal.getValue().toString() + task.getDescription() + " failed\n");
        }

        return null;
    }

}
