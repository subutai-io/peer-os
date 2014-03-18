package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec;

import com.vaadin.ui.TextArea;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.Wizard;

/**
 *
 * @author bahadyr
 */
public class ServiceInstaller implements TaskCallback {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;
    OozieConfig config;
    Wizard wizard;

    public ServiceInstaller(Wizard wizard, TextArea terminal) {
        this.terminal = terminal;
        this.config = wizard.getConfig();
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

        Set<Agent> allHadoopNodes = new HashSet<Agent>();
        allHadoopNodes.add(config.getServer());
        allHadoopNodes.addAll(config.getClients());

        Task configugeRootHostClients = new Task("Configure client");
        for (Agent agent : allHadoopNodes) {
            Request command = oc.getSetRootHost(" " + config.getServer().getListIP().get(0));
            command.setUuid(agent.getUuid());
            configugeRootHostClients.addRequest(command);
        }
        tasks.add(configugeRootHostClients);

        Task configugeRootGroupsClients = new Task("Configure client");
        for (Agent agent : allHadoopNodes) {
            Request command = oc.getSetRootGroups();
            command.setUuid(agent.getUuid());
            configugeRootGroupsClients.addRequest(command);
        }
        tasks.add(configugeRootGroupsClients);

    }

    public void start() {
        terminal.setValue("Starting installation...\n");
        moveToNextTask();
        if (currentTask != null) {
            OozieModule.getTaskRunner().executeTask(currentTask, this);
        }
    }

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

    private void saveInfo() {
        terminal.setValue(terminal.getValue().toString() + config.getUuid() + " saving cluster info.\n");
        if (wizard.getOozieDAO().saveClusterInfo(config)) {
            terminal.setValue(terminal.getValue().toString() + config.getUuid() + " cluster saved into keyspace.\n");
        } else {
            terminal.setValue(terminal.getValue().toString() + config.getUuid() + " cluster is not saved into keyspace.\n");
        }
    }

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
