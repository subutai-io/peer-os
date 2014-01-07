/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.InstallerConfig;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class Wizard implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(Wizard.class.getName());

    private static final int MAX_STEPS = 4;
    private final ProgressIndicator progressBar;
    private final VerticalLayout verticalLayout;
    private int step = 1;
    private final InstallerConfig mongoConfig = new InstallerConfig();
    private final VerticalLayout contentRoot;
    private Step5 step5;

    public Wizard() {
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

    protected void init() {
        step = 1;
        putForm();
    }

    public InstallerConfig getConfig() {
        return mongoConfig;
    }

    private void putForm() {
        verticalLayout.removeAllComponents();
        switch (step) {
            case 1: {
                progressBar.setValue(0f);
                verticalLayout.addComponent(new Step1(this));
                break;
            }
            case 2: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new Step2(this));
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new Step3(this));
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new Step4(this));
                break;
            }
            case 5: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                step5 = new Step5(this);
                verticalLayout.addComponent(step5);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onResponse(Response response) {
        if (step == 5 && step5 != null) {
            step5.onResponse(response);
        }

    }

    @Override
    public String getSource() {
        return MongoModule.MODULE_NAME;
    }

}
