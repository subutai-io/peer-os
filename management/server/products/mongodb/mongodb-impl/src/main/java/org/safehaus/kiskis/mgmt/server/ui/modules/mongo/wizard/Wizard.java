/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ProgressIndicator;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class Wizard {

    private static final Logger LOG = Logger.getLogger(Wizard.class.getName());

    private static final int MAX_STEPS = 4;
    private final ProgressIndicator progressBar;
    private final ClusterConfig config = new ClusterConfig();
    private final GridLayout grid;
    private int step = 1;

    public Wizard() {
        grid = new GridLayout(1, 20);
        grid.setMargin(true);
        grid.setSizeFull();

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        progressBar.setValue(0f);
        progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        grid.addComponent(progressBar, 0, 0, 0, 0);
        grid.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

        putForm();

    }

    public Component getContent() {
        return grid;
    }

    protected void next() {
        step++;
        putForm();
    }

    protected void back() {
        step--;
        putForm();
    }

    protected void init() {
        step = 1;
        putForm();
    }

    public ClusterConfig getConfig() {
        return config;
    }

    private void putForm() {
        grid.removeComponent(0, 1);
        Component component = null;
        switch (step) {
            case 1: {
                progressBar.setValue(0f);
                component = new WelcomeStep(this);
                break;
            }
            case 2: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                component = new ConfigNRoutersStep(this);
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                component = new ReplicaSetStep(this);
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                component = new VerifyStep(this);
                break;
            }
            case 5: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                component = new InstallationStep(this);
                ((InstallationStep) component).startOperation(true);
                break;
            }
            default: {
                break;
            }
        }

        if (component != null) {
            grid.addComponent(component, 0, 1, 0, 19);
        }
    }

}
