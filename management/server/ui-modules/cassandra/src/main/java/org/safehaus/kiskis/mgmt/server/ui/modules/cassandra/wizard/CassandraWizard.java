/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CassandraWizard {

    private static final int MAX_STEPS = 3;
    private final VerticalLayout verticalLayout;
    private int step = 1;
    private final CassandraConfig config = new CassandraConfig();
    private StepFinish stepFinish;
    GridLayout grid;

    public CassandraWizard() {
        verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        grid = new GridLayout(1, 1);
        grid.setMargin(true);
        grid.setSizeFull();
        grid.addComponent(verticalLayout);
        grid.setComponentAlignment(verticalLayout, Alignment.TOP_CENTER);

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
                verticalLayout.addComponent(new StepStart(this));
                break;
            }
            case 2: {
                verticalLayout.addComponent(new StepSeeds(this));
                break;
            }
            case 3: {
                verticalLayout.addComponent(new StepSetDirectories(this));
                break;
            }
            case 4: {
                stepFinish = new StepFinish(this);
                verticalLayout.addComponent(stepFinish);
                break;
            }
            default: {
                step = 1;
                verticalLayout.addComponent(new StepStart(this));
                break;
            }
        }
    }

    public void setOutput(Response response) {
        if (stepFinish != null) {
            stepFinish.getInstaller().onResponse(response);
        }
    }

}
