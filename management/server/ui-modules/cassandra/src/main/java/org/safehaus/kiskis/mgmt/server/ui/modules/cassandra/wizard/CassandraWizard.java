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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

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

    protected CassandraConfig getConfig() {
        return config;
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
                verticalLayout.addComponent(new StepListenRPC(this));
                break;
            }
            case 3: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new StepSeeds(this));
                break;
            }
            case 4: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new StepSetClusterName(this));
                break;
            }
            case 5: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new StepSetDirectories(this));
                break;
            }
            case 6: {
                progressBar.setValue((float) (step - 1) / MAX_STEPS);
                verticalLayout.addComponent(new Step3(this));
                break;
            }
            default: {
                break;
            }
        }
    }

    public static CommandManagerInterface getCommandManager() {
        BundleContext ctx = FrameworkUtil.getBundle(CassandraWizard.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

}
