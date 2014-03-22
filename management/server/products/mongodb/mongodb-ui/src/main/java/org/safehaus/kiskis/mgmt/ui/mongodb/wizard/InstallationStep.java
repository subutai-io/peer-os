/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.wizard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import java.util.UUID;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 *
 */
public class InstallationStep extends Panel {

    private static final Logger LOG = Logger.getLogger(InstallationStep.class.getName());
    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button done;
    private final Button back;
    private final Label indicator;
    private final Config config;

    public InstallationStep(final Wizard wizard) {

        this.config = wizard.getConfig();

        setSizeFull();

        GridLayout content = new GridLayout(1, 3);
        content.setSizeFull();
        content.setMargin(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows(13);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        logTextArea = new TextArea("Node output");
        logTextArea.setSizeFull();
        logTextArea.setRows(13);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        content.addComponent(logTextArea);

        done = new Button("Done");
        done.setEnabled(false);
        done.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
            }
        });
        back = new Button("Back");
        back.setEnabled(false);
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.addComponent(back);
        bottomContent.addComponent(done);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);

    }

    public void startOperation() {
        showProgress();
        MongoUI.getExecutor().execute(new Runnable() {

            public void run() {
                UUID operationID = MongoUI.getMongoManager().installCluster(config);
                if (operationID != null) {

                    while (!Thread.interrupted()) {
                        try {
                            ProductOperationView po = MongoUI.getDbManager().getProductOperation(operationID);
                            if (po != null) {
                                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                                if (po.getState() == ProductOperationState.FAILED || po.getState() == ProductOperationState.SUCCEEDED) {
                                    hideProgress();
                                    return;
                                }
                            } else {
                                setOutput("Product operation not found. Check logs");
                            }
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {

                        }
                    }
                } else {
                    setOutput("Could not get operation output. Check logs");
                }
            }
        });
    }

    private void showProgress() {
        indicator.setVisible(true);
        done.setEnabled(false);
        back.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        done.setEnabled(true);
        back.setEnabled(true);
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
