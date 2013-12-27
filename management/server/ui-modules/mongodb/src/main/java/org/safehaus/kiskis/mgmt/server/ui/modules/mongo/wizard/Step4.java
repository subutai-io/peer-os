/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Installer;
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
    private final Installer installer;

    public Step4(final MongoWizard mongoWizard) {

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

        Button ok = new Button("OK");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongoWizard.init();
            }
        });
        
        //ADD CANCEL BUTTON WITH UNINSTALLATION

        content.addComponent(ok);
        addComponent(content);

        installer = new Installer(mongoWizard);
        currentTask = installer.start();
        outputTxtArea.setValue(MessageFormat.format("Running task {0}...", currentTask.getDescription()));
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    protected void onResponse(Response response) {
        if (currentTask != null && response != null
                && currentTask.getUuid() != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {
            CommandManagerInterface commandManager = ServiceLocator.getService(CommandManagerInterface.class);
            int count = commandManager.getResponseCount(currentTask.getUuid());
            if (currentTask.getCommands().size() == count) {
                int okCount = commandManager.getSuccessfullResponseCount(currentTask.getUuid());
                String prevTaskDescription = currentTask.getDescription();
                if (count == okCount
                        || currentTask.getDescription().equalsIgnoreCase(Constants.MONGO_UNINSTALL_TASK_NAME)) {
                    currentTask.setTaskStatus(TaskStatus.SUCCESS);
                    commandManager.saveTask(currentTask);
                    currentTask = installer.executeNextTask();
                    if (currentTask != null) {
                        outputTxtArea.setValue(
                                MessageFormat.format("{0}\n\nTask {1} succeeded\n\nRunning next task {2}...",
                                        outputTxtArea.getValue(),
                                        prevTaskDescription,
                                        currentTask.getDescription()));
                    } else {
                        outputTxtArea.setValue(
                                MessageFormat.format("{0}\n\nTask {1} succeeded.\n\nInstallation completed successfully.",
                                        outputTxtArea.getValue(),
                                        prevTaskDescription));
                    }
                } else {
                    currentTask.setTaskStatus(TaskStatus.FAIL);
                    commandManager.saveTask(currentTask);
                    currentTask = null;
                    outputTxtArea.setValue(
                            MessageFormat.format("{0}\n\nTask {1} failed.\n\nInstallation aborted.",
                                    outputTxtArea.getValue(),
                                    prevTaskDescription));
                    //PROBABLY RUN HERE UNINSTALL COMMAND
                }
                outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
            }
        }

    }
}
