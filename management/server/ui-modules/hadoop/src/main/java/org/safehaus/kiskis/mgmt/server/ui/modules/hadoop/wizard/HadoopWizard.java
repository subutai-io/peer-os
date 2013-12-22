package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util.HadoopInstallation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

import java.util.List;

public final class HadoopWizard extends Window {

    private final VerticalLayout verticalLayout;
    HadoopInstallation hadoopInstallation;
    private List<Agent> lxcList;

    private final ProgressIndicator progressBar;
    private static final int MAX_STEPS = 3;

    Step1 step1;
    Step2 step2;
    Step3 step3;
    int step = 1;

    public HadoopWizard(List<Agent> lxcList) {
        setModal(true);
        hadoopInstallation = new HadoopInstallation(getCommandManager());

        this.lxcList = lxcList;
        setCaption("HadoopModule Wizard");

        GridLayout gridLayout = new GridLayout(1, 15);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(false, true, false, true);
        gridLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.setWidth(900, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        progressBar.setPollingInterval(30000);
        progressBar.setValue(0f);
        progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(progressBar, 0, 0);
        gridLayout.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.addComponent(verticalLayout, 0, 1, 0, 14);
        gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);

        putForm();

        setContent(gridLayout);
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
                this.setClosable(false);
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            default: {
                this.close();
                break;
            }
        }
    }

    public void setOutput(Response response) {
        hadoopInstallation.onCommand(response, step3);
    }

    public HadoopInstallation getHadoopInstallation() {
        return hadoopInstallation;
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
