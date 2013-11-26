package org.safehaus.kiskis.mgmt.server.ui.modules.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.Cassandra;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

public final class CassandraWizard extends Window {

    private final CommandManagerInterface commandManagerInterface;

    VerticalLayout verticalLayout;
    Task task;
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
        setCaption("Cassandra Wizard");

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setHeight(400, Sizeable.UNITS_PIXELS);
        verticalLayout.setWidth(800, Sizeable.UNITS_PIXELS);

        putForm();

        setContent(verticalLayout);
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
                step1 = new Step1(this);
                verticalLayout.addComponent(step1);
                break;
            }
            case 2: {
                step2 = new Step2(this);
                verticalLayout.addComponent(step2);
                break;
            }
            case 3: {
                step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            case 4: {
                step41 = new Step41(this);
                verticalLayout.addComponent(step41);
                break;
            }
            case 5: {
                step42 = new Step42(this);
                verticalLayout.addComponent(step42);
                break;
            }
            case 6: {
                step43 = new Step43(this);
                verticalLayout.addComponent(step43);
                break;
            }
            default: {
                step = 1;
                step1 = new Step1(this);
                verticalLayout.addComponent(step1);
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

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(Cassandra.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
