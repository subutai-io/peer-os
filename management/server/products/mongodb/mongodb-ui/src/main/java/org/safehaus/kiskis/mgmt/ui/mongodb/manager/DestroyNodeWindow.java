/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.manager;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class DestroyNodeWindow extends Window {

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final Config config;
    private final Agent agent;
    private volatile boolean track = true;

    public DestroyNodeWindow(Config config, Agent agent) {
        super("Destroy node");
        setModal(true);
        setClosable(false);

        this.config = config;
        this.agent = agent;

        setWidth(600, DestroyNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                track = false;
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);

        start();
    }

    @Override
    protected void close() {
        super.close();
        track = false;
    }

    private void start() {
        MongoUI.getExecutor().execute(new Runnable() {

            public void run() {
                showProgress();
                MongoUI.getExecutor().execute(new Runnable() {

                    public void run() {
                        UUID operationID = MongoUI.getMongoManager().destroyNode(config.getClusterName(), agent.getHostname());
                        while (track) {
                            ProductOperationView po = MongoUI.getDbManager().getProductOperation(
                                    Config.PRODUCT_KEY, operationID);
                            if (po != null) {
                                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                                if (po.getState() != ProductOperationState.RUNNING) {
                                    hideProgress();
                                    return;
                                }
                            } else {
                                setOutput("Product operation not found. Check logs");
                            }
                        }
                    }
                });
            }

        });

    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
