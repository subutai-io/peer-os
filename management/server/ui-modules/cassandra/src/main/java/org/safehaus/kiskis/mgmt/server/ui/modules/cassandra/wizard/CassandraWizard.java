/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CassandraWizard {

    private static final int MAX_STEPS = 3;
    private final ProgressIndicator progressBar;
    private final VerticalLayout verticalLayout;
    private int step = 1;
    private final CassandraConfig config = new CassandraConfig();
    private final VerticalLayout contentRoot;
    public static final String SOURCE = "CASSANDRA_WIZARD";
    private StepFinish stepFinish;

    public CassandraWizard() {
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        GridLayout content = new GridLayout(1, 2);
        content.setSpacing(true);
//            gridLayout.setMargin(false, true, true, true);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setWidth(900, Sizeable.UNITS_PIXELS);

        progressBar = new ProgressIndicator();
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        progressBar.setValue(0f);
        progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.addComponent(progressBar, 0, 0);
        content.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.addComponent(verticalLayout, 0, 1);
        content.setComponentAlignment(verticalLayout, Alignment.TOP_CENTER);

        contentRoot.addComponent(content);
        contentRoot.setMargin(true);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);

        putForm();

    }

    public Component getContent() {
        return contentRoot;
    }

    protected void next() {
        step++;
        putForm();
    }

    protected void back() {
        step--;
        putForm();
    }

    protected void cancel() {
        step = 1;
        putForm();
    }

    protected CassandraConfig getConfig() {
        return config;
    }

    private void putForm() {
        verticalLayout.removeAllComponents();
        switch (step) {
            case 1: {
                progressBar.setValue(0f);
                verticalLayout.addComponent(new StepStart(this));
                break;
            }
            case 2: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new StepSeeds(this));
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new StepSetDirectories(this));
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                stepFinish = new StepFinish(this);
                verticalLayout.addComponent(stepFinish);
                break;
            }
            default: {
                step = 1;
                progressBar.setValue(0f);
                verticalLayout.addComponent(new StepStart(this));
                break;
            }
        }
    }

    public void setOutput(Response response) {
        stepFinish.getInstaller().onResponse(response);
    }

}
