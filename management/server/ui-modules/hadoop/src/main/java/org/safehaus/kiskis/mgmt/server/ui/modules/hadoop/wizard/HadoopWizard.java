package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

import java.util.List;

public final class HadoopWizard extends Window {

    private final CommandManagerInterface commandManagerInterface;

    private final VerticalLayout verticalLayout;
    private Task task;
    private String clusterName;
    private List<Agent> lxcList;
    private IndexedContainer container;

    private final TextArea textAreaTerminal;
    private final ProgressIndicator progressBar;
    private static final int MAX_STEPS = 2;

    Step1 step1;
    Step2 step2;
    int step = 1;

    public HadoopWizard(List<Agent> lxcList) {
        setModal(true);

        this.lxcList = lxcList;
        this.commandManagerInterface = getCommandManager();
        setCaption("HadoopModule Wizard");

        GridLayout gridLayout = new GridLayout(1, 15);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(false, true, false, true);
        gridLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.setWidth(900, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(true);
        progressBar.setPollingInterval(30000);
        progressBar.setValue(0f);
        progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(progressBar, 0, 0);
        gridLayout.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(verticalLayout, 0, 1, 0, 13);
        gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);

        textAreaTerminal = new TextArea();
        textAreaTerminal.setRows(5);
        textAreaTerminal.setColumns(65);
        textAreaTerminal.setImmediate(true);
        textAreaTerminal.setWordwrap(true);
        gridLayout.addComponent(textAreaTerminal, 0, 14);
        gridLayout.setComponentAlignment(textAreaTerminal, Alignment.MIDDLE_CENTER);

        putForm();

        setContent(gridLayout);
    }

    public void runCommand(Command command) {
        commandManagerInterface.executeCommand(command);
    }

    public void showNext() {
        step++;
        putForm();
    }

    public void showBack() {
        step--;
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
            default: {
                this.close();
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

    public void setOutput(Response response) {
        if(task != null){
            if (response.getTaskUuid().equals(task.getUuid().toString())) {
                StringBuilder output = new StringBuilder();
                output.append(textAreaTerminal.getValue());
                if (response.getStdErr() != null && response.getStdErr().trim().length() != 0) {
                    output.append("ERROR ").append(response.getStdErr().trim());
                }
                if (response.getStdOut() != null && response.getStdOut().trim().length() != 0) {
                    output.append("OK ").append(response.getStdOut().trim());
                }
                switch (step) {
                    case 1: {
                        break;
                    }
                    case 2: {
                        break;
                    }

                }

                textAreaTerminal.setValue(output);
                textAreaTerminal.setCursorPosition(output.length() - 1);
            }
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<Agent> getLxcList() {
        return lxcList;
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
