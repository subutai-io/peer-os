package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

public final class CassandraWizard {

    private final CommandManagerInterface commandManagerInterface;
    private CassandraConfig config = new CassandraConfig();
    private final VerticalLayout verticalLayout;
    private Task task;
    private CassandraClusterInfo cluster;
//    private final List<Agent> lxcList;
    private final ProgressIndicator progressBar;
    private static final int MAX_STEPS = 5;
    GridLayout gridLayout;

    Step1 step1;
    Step2 step2;
    Step3 step3;
    Step4 step4;
    Step5 step5;
    Step6 step6;
    int step = 1;

    /**
     *
     */
    public CassandraWizard() {
//        setModal(true);
//        this.lxcList = lxcList;
        this.commandManagerInterface = getCommandManager();
//        setCaption("Cassandra Wizard");

        gridLayout = new GridLayout(1, 10);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(false, true, false, true);
        gridLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        gridLayout.setWidth(920, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        progressBar.setPollingInterval(30000);
        progressBar.setValue(0f);
        progressBar.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(progressBar, 0, 0);
        gridLayout.setComponentAlignment(progressBar, Alignment.TOP_CENTER);

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(verticalLayout, 0, 1, 0, 8);
        gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);

        putForm();

    }

    public void runCommand(Command command) {
        commandManagerInterface.executeCommand(command);
    }

    public void showNext() {
        step++;
        putForm();
    }

    public void cancelWizard() {
        for (Agent agent : MgmtApplication.getSelectedAgents()) {
            int reqSeqNumber = task.getIncrementedReqSeqNumber();
            UUID taskUuid = task.getUuid();
            List<String> args = new ArrayList<String>();
            String purgeCommand = "apt-get --force-yes --assume-yes purge ksks-cassandra";
            Command command = buildCommand(agent.getUuid(), purgeCommand, reqSeqNumber, taskUuid, args);
            commandManagerInterface.executeCommand(command);
        }
        step = 1;
        putForm();
    }

    private void putForm() {
        verticalLayout.removeAllComponents();
        switch (step) {
            case 1: {
                progressBar.setValue(0f);
                step1 = new Step1(this);
                verticalLayout.addComponent(step1);
                break;
            }
            case 2: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step2 = new Step2(this);
                verticalLayout.addComponent(step2);
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step4 = new Step4(this);
                verticalLayout.addComponent(step4);
                break;
            }
            case 5: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step5 = new Step5(this);
                verticalLayout.addComponent(step5);
                break;
            }
            case 6: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step6 = new Step6(this);
                verticalLayout.addComponent(step6);
                break;
            }
            case 7: {
                cluster.setNodes(getAgentsUUIDS());
                commandManagerInterface.saveCassandraClusterData(cluster);
                task.setTaskStatus(TaskStatus.SUCCESS);
                commandManagerInterface.saveTask(task);
                step = 1;
                putForm();
            }
            default: {
//                this.close();
//                removeWindow(this);
                break;
            }
        }
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public CassandraClusterInfo getCluster() {
        return cluster;
    }

    public void setCluster(CassandraClusterInfo cluster) {
        this.cluster = cluster;
    }

    public void setOutput(Response response) {
        if (task != null && response.getTaskUuid().toString().equals(task.getUuid().toString())) {
//            StringBuilder output = new StringBuilder();
//            output.append(terminal.getValue());
            if (response.getStdErr() != null && response.getStdErr().trim().length() != 0) {
//                output.append("ERROR ").append(response.getStdErr().trim());
            }
            if (response.getStdOut() != null && response.getStdOut().trim().length() != 0) {
//                output.append("OK ").append(response.getStdOut().trim());
            }

            switch (response.getType()) {
                case EXECUTE_RESPONSE_DONE: {
                    switch (step) {
                        case 1: {
                            break;
                        }
                        case 2: {
//                            terminal.setValue("Step 2");
                            break;
                        }
                        case 3: {
//                            for (ParseResult pr : commandManagerInterface.parseTask(task, true)) {
//                                terminal.setValue("OUTPUT" + pr.getResponse().getStdOut());
//                            }
                            break;
                        }
                        case 4: {
                            break;
                        }
                        case 5: {
//                            step5.updateUI(response.getExitCode() == 0 ? "Success" : "Fail");
                            break;
                        }
                        case 6: {
                            break;
                        }
                    }
                }
            }

//            terminal.setValue(output);
//            terminal.setCursorPosition(output.length() - 1);
        }
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(CassandraModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

//    public List<Agent> getLxcList() {
//        return lxcList;
//    }
    private Command buildCommand(UUID uuid, String program, int reqSeqNumber, UUID taskUuid, List<String> args) {
        return (Command) CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST,
                uuid,
                CassandraModule.MODULE_NAME,
                taskUuid,
                reqSeqNumber,
                "/",
                program,
                OutputRedirection.RETURN,
                OutputRedirection.RETURN,
                null,
                null,
                "root",
                args,
                null,
                null);
    }

    private List<UUID> getAgentsUUIDS() {
        List<UUID> uuids = new ArrayList<UUID>();
        for (Agent agent : MgmtApplication.getSelectedAgents()) {
            uuids.add(agent.getUuid());
        }
        return uuids;
    }

    public Component getContent() {
        return gridLayout;
    }

    public CassandraConfig getConfig() {
        return config;
    }

}
