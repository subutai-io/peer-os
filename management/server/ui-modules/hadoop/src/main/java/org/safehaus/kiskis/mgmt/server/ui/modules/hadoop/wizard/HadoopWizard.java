package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Installation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.List;

public final class HadoopWizard {

    private static final int MAX_STEPS = 4;
    private final GridLayout contentRoot;
    private final VerticalLayout verticalLayout;
    private final ProgressIndicator progressBar;
    Installation hadoopInstallation;
    Step0 step0;
    Step1 step1;
    Step2 step2;
    Step3 step3;
    int step = 0;

    public HadoopWizard() {
        hadoopInstallation = new Installation();

        contentRoot = new GridLayout(1, 15);
        contentRoot.setSpacing(true);
        contentRoot.setMargin(false, true, false, true);
        contentRoot.setHeight(600, Sizeable.UNITS_PIXELS);
        contentRoot.setWidth(900, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        progressBar.setPollingInterval(30000);
        progressBar.setValue(0f);
        progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        contentRoot.addComponent(progressBar, 0, 0);
        contentRoot.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        contentRoot.addComponent(verticalLayout, 0, 1, 0, 14);
        contentRoot.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);

        putForm();
    }

    public Component getContent() {
        return contentRoot;
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
            case 0: {
                progressBar.setValue((float) step / MAX_STEPS);
                step0 = new Step0(this);
                verticalLayout.addComponent(step0);
                break;
            }
            case 1: {
                progressBar.setValue((float) step / MAX_STEPS);
                step1 = new Step1(this);
                verticalLayout.addComponent(step1);
                break;
            }
            case 2: {
                progressBar.setValue((float) step / MAX_STEPS);
                step2 = new Step2(this);
                verticalLayout.addComponent(step2);
                break;
            }
            case 3: {
                progressBar.setValue((float) step / MAX_STEPS);
                step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            default: {
                step = 0;
                progressBar.setValue((float) step / MAX_STEPS);
                step0 = new Step0(this);
                verticalLayout.addComponent(step0);
                break;
            }
        }
    }

    public Installation getHadoopInstallation() {
        return hadoopInstallation;
    }

    public List<Agent> getLxcList() {
        List<Agent> list = new ArrayList<Agent>();
        if (MgmtApplication.getSelectedAgents() != null && !MgmtApplication.getSelectedAgents().isEmpty()) {
            for (Agent agent : MgmtApplication.getSelectedAgents()) {
                if (agent.isIsLXC()) {
                    list.add(agent);
                }
            }
        }

        return list;
    }
}
