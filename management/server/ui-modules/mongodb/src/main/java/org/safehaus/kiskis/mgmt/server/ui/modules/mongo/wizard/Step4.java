/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Installer;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Uninstaller;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class Step4 extends Panel {

    private static final Logger LOG = Logger.getLogger(Step4.class.getName());

    private final TextArea outputTxtArea;
    private Task currentTask = null;
    private Operation operation;
    private final Button ok;
    private final Button cancel;
    private final Label indicator;
    private final CommandManagerInterface commandManager;
    private boolean isInProgress = false;
    private Thread operationTimeoutThread;

    public Step4(final Wizard wizard) {
        commandManager
                = ServiceLocator.getService(CommandManagerInterface.class);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        outputTxtArea = new TextArea("Installation output");
        outputTxtArea.setRows(20);
        outputTxtArea.setColumns(100);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        ok = new Button("OK");
        ok.setEnabled(false);
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
            }
        });
        cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cancel.setEnabled(false);
                startOperation(wizard, Uninstaller.class);
            }
        });

        HorizontalLayout footer = new HorizontalLayout();
        footer.addComponent(ok);
        footer.addComponent(cancel);
        content.addComponent(footer);

        indicator = Util.createImage("indicator.gif", 50, 50);
        content.addComponent(indicator);

        addComponent(content);

        startOperation(wizard, Installer.class);
    }

    private void startOperation(final Wizard wizard, Class operationClass) {
        try {
            currentTask = null;
            operation = (Operation) operationClass.getConstructor(Wizard.class).newInstance(wizard);
            currentTask = operation.executeNextTask();
            outputTxtArea.setValue(MessageFormat.format(
                    "{0}\n\nOperation \"{1}\" started.\n\nRunning task {2}...",
                    outputTxtArea.getValue(),
                    operation.getDescription(),
                    currentTask.getDescription()));
            showProgress();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in startOperation", ex);
        }
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private void showProgress() {
        isInProgress = true;
        final int operationTimeout = operation.getOverallTimeout();
        if (operationTimeoutThread != null && operationTimeoutThread.isAlive()) {
            operationTimeoutThread.interrupt();
        }
        operationTimeoutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //wait for overalltimeout + 5 sec just in case
                    Thread.sleep(operationTimeout * 1000 + 5000);
                    if (isInProgress) {
                        show("Operation timeouted!!!");
                        hideProgress();
                    }
                } catch (InterruptedException ex) {
                }
            }
        });
        operationTimeoutThread.start();
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        isInProgress = false;
        indicator.setVisible(false);
        ok.setEnabled(true);
        cancel.setEnabled(true);
    }

    protected void onResponse(Response response) {
        if (currentTask != null && response != null
                && currentTask.getUuid() != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {

            int count = commandManager.getResponseCount(currentTask.getUuid());
            if (currentTask.getCommands().size() == count) {
                int okCount = commandManager.getSuccessfullResponseCount(currentTask.getUuid());
                //task completed
                String prevTaskDescription = currentTask.getDescription();
                if (count == okCount
                        || currentTask.getDescription().equalsIgnoreCase(
                                Constants.MONGO_UNINSTALL_TASK_NAME)) {
                    //task succeeded
                    currentTask.setTaskStatus(TaskStatus.SUCCESS);
                    commandManager.saveTask(currentTask);
                    currentTask = null;
                    if (operation.hasMoreTasks()) {
                        currentTask = operation.executeNextTask();
                        outputTxtArea.setValue(
                                MessageFormat.format(
                                        "{0}\n\nTask {1} succeeded.\n\nRunning next task {2}...",
                                        outputTxtArea.getValue(),
                                        prevTaskDescription,
                                        currentTask.getDescription()));
                    } else {
                        outputTxtArea.setValue(
                                MessageFormat.format(
                                        "{0}\n\nTask {1} succeeded.\n\nOperation \"{2}\" completed successfully.",
                                        outputTxtArea.getValue(),
                                        prevTaskDescription,
                                        operation.getDescription()));
                        hideProgress();
                    }
                } else {
                    //task failed
                    currentTask.setTaskStatus(TaskStatus.FAIL);
                    commandManager.saveTask(currentTask);
                    currentTask = null;
                    outputTxtArea.setValue(
                            MessageFormat.format("{0}\n\nTask {1} failed.\n\nOperation \"{2}\" aborted.",
                                    outputTxtArea.getValue(),
                                    prevTaskDescription,
                                    operation.getDescription()));
                    hideProgress();
                }
                outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
            }
        }

    }
}
