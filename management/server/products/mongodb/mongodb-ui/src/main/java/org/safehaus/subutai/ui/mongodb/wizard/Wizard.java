/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mongodb.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ProgressIndicator;
import org.safehaus.subutai.api.mongodb.Config;

/**
 *
 * @author dilshat
 */
public class Wizard {

    private static final int NUMBER_OF_STEPS = 3;
    private final ProgressIndicator progressBar;
    private final GridLayout grid;
    private int step = 1;
    private Config config = new Config();

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
        config = new Config();
        putForm();
    }

    public Config getConfig() {
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
                progressBar.setValue((float) (step - 1) / (NUMBER_OF_STEPS - 1));
                component = new ConfigurationStep(this);
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / (NUMBER_OF_STEPS - 1));
                component = new VerificationStep(this);
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
