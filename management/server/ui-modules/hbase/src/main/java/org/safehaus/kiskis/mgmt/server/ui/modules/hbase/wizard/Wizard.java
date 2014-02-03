/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.Config;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class Wizard {

    private static final int MAX_STEPS = 3;
    private final VerticalLayout vlayout;
    private int step = 1;
    private final Config config = new Config();
    private StepFinish stepFinish;

    public Wizard() {
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        vlayout.setMargin(true);
        putForm();
    }

    public Component getContent() {
        return vlayout;
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

    protected Config getConfig() {
        return config;
    }

    private void putForm() {
        vlayout.removeAllComponents();
        switch (step) {
            case 1: {
                vlayout.addComponent(new StepStart(this));
                break;
            }
            case 2: {
                vlayout.addComponent(new StepSetMaster(this));
                break;
            }
            case 3: {
                vlayout.addComponent(new StepSetRegion(this));
                break;
            }
            case 4: {
                vlayout.addComponent(new StepSetQuorum(this));
                break;
            }
            case 5: {
                vlayout.addComponent(new StepSetBackuUpMasters(this));
                break;
            }
            case 6: {
                stepFinish = new StepFinish(this);
                vlayout.addComponent(stepFinish);
                break;
            }
            default: {
                step = 1;
                vlayout.addComponent(new StepStart(this));
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
