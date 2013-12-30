/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Util.createImage;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Installer;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Uninstaller;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class Step4 extends Panel implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(Step4.class.getName());

    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private Operation operation;
    private final Button ok;
    private final Button revert;
    private final Label indicator;
    private Thread operationTimeoutThread;

    public Step4(final Wizard wizard) {

//        VerticalLayout content = new VerticalLayout();
//        content.setSizeFull();
//        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
//        content.setMargin(true);
        GridLayout content = new GridLayout(9, 9);
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        outputTxtArea = new TextArea("Installation output");
        outputTxtArea.setRows(20);
        outputTxtArea.setColumns(50);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 0, 8, 3);

        ok = new Button("OK");
        ok.setEnabled(false);
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
            }
        });
        revert = new Button("Revert");
        revert.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                revert.setEnabled(false);
                startOperation(new Uninstaller(wizard.getConfig()));
            }
        });

        indicator = createImage("indicator.gif", 50, 50);

        content.addComponent(ok, 0, 4, 0, 4);
        content.addComponent(revert, 1, 4, 1, 4);
        content.addComponent(indicator, 8, 4, 8, 4);

        logTextArea = new TextArea("Command output");
        logTextArea.setRows(20);
        logTextArea.setColumns(50);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);
        
        content.addComponent(logTextArea, 0, 5, 8, 8);

        addComponent(content);

        startOperation(new Installer(wizard.getConfig()));
    }

    private void startOperation(final Operation operation) {
        if (operation != null) {
            try {
                //stop any running operation
                if (this.operation != null) {
                    this.operation.stop();
                }
                this.operation = operation;
                if (operation.start()) {
                    showProgress();
                    outputTxtArea.setValue(MessageFormat.format(
                            "{0}\n\nOperation \"{1}\" started.\n\nRunning task {2}...",
                            outputTxtArea.getValue(),
                            operation.getDescription(),
                            operation.getCurrentTask().getDescription()));
                    if (operationTimeoutThread != null && operationTimeoutThread.isAlive()) {
                        operationTimeoutThread.interrupt();
                    }
                    operationTimeoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //wait for overalltimeout + 5 sec just in case
                                Thread.sleep(operation.getOverallTimeout() * 1000 + 5000);
                                if (!operation.isStopped()
                                        && !operation.isFailed()
                                        && !operation.isCompleted()) {
                                    outputTxtArea.setValue(
                                            MessageFormat.format("{0}\n\nOperation {1} timeouted!!!",
                                                    outputTxtArea.getValue(),
                                                    operation.getDescription()));
                                    hideProgress();
                                }
                            } catch (InterruptedException ex) {
                            }
                        }
                    });
                    operationTimeoutThread.start();
                } else {
                    outputTxtArea.setValue(MessageFormat.format(
                            "{0}\n\nOperation \"{1}\" could not be started: {2}.",
                            outputTxtArea.getValue(),
                            operation.getDescription(),
                            operation.getOutput()));
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in startOperation", ex);
            }
        }
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
        revert.setEnabled(true);
    }

    @Override
    public void onResponse(Response response) {
        if (operation != null) {
            try {
                operation.onResponse(response);
                String output = operation.getOutput();
                if (!Util.isStringEmpty(output)) {
                    outputTxtArea.setValue(
                            MessageFormat.format("{0}\n\n{1}",
                                    outputTxtArea.getValue(),
                                    output));
                    outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
                }
                String log = operation.getLog();
                if (!Util.isStringEmpty(log)) {
                    logTextArea.setValue(
                            MessageFormat.format("{0}\n\n{1}",
                                    logTextArea.getValue(),
                                    log));
                    logTextArea.setCursorPosition(logTextArea.getValue().toString().length() - 1);
                }
                if (operation.isCompleted() || operation.isStopped() || operation.isFailed()) {
                    hideProgress();
                }

            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in onResponse", e);
            }
        }

    }

    @Override
    public String getSource() {
        return getClass().getName();
    }
}
