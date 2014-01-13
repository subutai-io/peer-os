/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.Uninstaller;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class DestroyWindow extends Window implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(DestroyWindow.class.getName());

    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button ok;
    private final Label indicator;
    private Thread operationTimeoutThread;
    private Operation operation;

    public DestroyWindow(String caption) {
        super(caption);
        setModal(true);

        setWidth(600, DestroyWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(20, 3);
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        outputTxtArea = new TextArea("Uninstallation output");
        outputTxtArea.setRows(17);
        outputTxtArea.setColumns(35);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 0, 18, 0);
        ok = new Button("Ok");
//        ok.setEnabled(false);
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 50);

        content.addComponent(ok, 0, 1, 0, 1);
        content.addComponent(indicator, 19, 0, 19, 0);
        content.setComponentAlignment(indicator, Alignment.TOP_RIGHT);

        logTextArea = new TextArea("Command output");
        logTextArea.setRows(17);
        logTextArea.setColumns(35);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        content.addComponent(logTextArea, 0, 2, 18, 2);

        addComponent(content);
    }

    public void startUninstallation(Set<Agent> clusterMembers) {
        startOperation(new Uninstaller(clusterMembers));
    }

    private void startOperation(final Operation operation) {
        if (operation != null) {
            try {
                //stop any running operation
                if (this.operation != null) {
                    this.operation.stop();
                    this.operation = null;
                }
                this.operation = operation;
                if (operation.start()) {
                    showProgress();
                    addOutput(operation.getOutput());
                    addLog(operation.getLog());
                    if (operationTimeoutThread != null && operationTimeoutThread.isAlive()) {
                        operationTimeoutThread.interrupt();
                    }
                    operationTimeoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //wait for overalltimeout + 15 sec just in case
                                Thread.sleep(operation.getTotalTimeout() * 1000 + 15000);
                                if (!operation.isStopped()
                                        && !operation.isFailed()
                                        && !operation.isSucceeded()) {
                                    addOutput(MessageFormat.format(
                                            "Operation \"{0}\" timeouted!!!",
                                            operation.getDescription()));
                                    hideProgress();
                                }
                            } catch (InterruptedException ex) {
                            }
                        }
                    });
                    operationTimeoutThread.start();
                } else {
                    this.operation = null;
                    addOutput(MessageFormat.format(
                            "Operation \"{0}\" could not be started: {1}.",
                            operation.getDescription(),
                            operation.getOutput()));
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in startOperation", ex);
            }
        }
    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    @Override
    public void onResponse(Response response) {
        if (operation != null) {
            try {
                operation.onResponse(response);
                addOutput(operation.getOutput());
                addLog(operation.getLog());
                if (operation.isSucceeded() || operation.isStopped() || operation.isFailed()) {
                    hideProgress();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in onResponse", e);
            }
        }

    }

    public boolean isSucceeded() {
        if (operation != null) {
            return operation.isSucceeded();
        }
        return false;
    }

    private void addOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            outputTxtArea.getValue(),
                            output));
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

    private void addLog(String log) {
        if (!Util.isStringEmpty(log)) {
            logTextArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            logTextArea.getValue(),
                            log));
            logTextArea.setCursorPosition(logTextArea.getValue().toString().length() - 1);
        }
    }

}
