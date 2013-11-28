package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

public final class CassandraWizard extends Window {

    private final CommandManagerInterface commandManagerInterface;

    private final VerticalLayout verticalLayout;
    private Task task;

    private final TextArea textAreaTerminal;
    private final ProgressIndicator progressBar;
    private static final int MAX_STEPS = 5;

    Step1 step1;
    Step2 step2;
    Step3 step3;
    Step41 step41;
    Step42 step42;
    Step43 step43;
    int step = 1;

    public CassandraWizard() {
        setModal(true);

        this.commandManagerInterface = getCommandManager();
        setCaption("CassandraModule Wizard");

        GridLayout gridLayout = new GridLayout(1, 10);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(false, true, false, true);
        gridLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        gridLayout.setWidth(920, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(true);
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

        textAreaTerminal = new TextArea();
        textAreaTerminal.setRows(10);
        textAreaTerminal.setColumns(65);
        textAreaTerminal.setImmediate(true);
        textAreaTerminal.setWordwrap(true);
        gridLayout.addComponent(textAreaTerminal, 0, 9);
        gridLayout.setComponentAlignment(textAreaTerminal, Alignment.TOP_CENTER);

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
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step41 = new Step41(this);
                verticalLayout.addComponent(step41);
                break;
            }
            case 5: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step42 = new Step42(this);
                verticalLayout.addComponent(step42);
                break;
            }
            case 6: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step43 = new Step43(this);
                verticalLayout.addComponent(step43);
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
                case 3: {
                    step3.updateUI(response.getStdOut() + " " + response.getStdErr());
                }
                case 4: {
                    break;
                }
                case 5: {
                    break;
                }
                case 6: {
                    break;
                }

            }

            textAreaTerminal.setValue(output);
            textAreaTerminal.setCursorPosition(output.length() - 1);
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
}
