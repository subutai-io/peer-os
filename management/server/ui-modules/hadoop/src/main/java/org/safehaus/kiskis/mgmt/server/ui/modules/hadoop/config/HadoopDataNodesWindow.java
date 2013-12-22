package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

public final class HadoopDataNodesWindow extends Window {

    private Button startButton, stopButton, restartButton;
    private Label statusLabel;


    public HadoopDataNodesWindow() {
        setModal(true);
        setCaption("Hadoop Data Node Configuration");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        buttonLayout.addComponent(getStartButton());
        buttonLayout.addComponent(getStopButton());
        buttonLayout.addComponent(getRestartButton());
        buttonLayout.addComponent(getStatusLabel());

        verticalLayout.addComponent(buttonLayout);
        setContent(verticalLayout);
    }

    private Button getStartButton(){
        startButton = new Button("Start");

        return startButton;
    }

    private Button getStopButton(){
        stopButton = new Button("Stop");

        return stopButton;
    }

    private Button getRestartButton(){
        restartButton = new Button("Restart");

        return restartButton;
    }

    private Label getStatusLabel(){
        statusLabel = new Label();
        statusLabel.setValue("");

        return statusLabel;
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
