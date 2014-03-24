/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class AddNodeWindow extends Window {

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final Config config;
    private volatile boolean track = true;

    public AddNodeWindow(final Config config) {
        super("Add New Node");
        setModal(true);
        setClosable(false);

        this.config = config;

        setWidth(600, AddNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(1, 3);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        content.addComponent(topContent);

        final ComboBox nodeTypeCombo = new ComboBox();
        nodeTypeCombo.setMultiSelect(false);
        nodeTypeCombo.setImmediate(true);
        nodeTypeCombo.setNullSelectionAllowed(false);
        nodeTypeCombo.setTextInputAllowed(false);
        nodeTypeCombo.setWidth(150, Sizeable.UNITS_PIXELS);

        nodeTypeCombo.addItem(NodeType.ROUTER_NODE);
        nodeTypeCombo.setItemCaption(NodeType.ROUTER_NODE, "Add Router");
        nodeTypeCombo.addItem(NodeType.DATA_NODE);
        nodeTypeCombo.setItemCaption(NodeType.DATA_NODE, "Add Data Node");
        nodeTypeCombo.setValue(NodeType.DATA_NODE);
        topContent.addComponent(nodeTypeCombo);

        final Button addNodeBtn = new Button("Add");
        topContent.addComponent(addNodeBtn);

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                NodeType nodeType = (NodeType) nodeTypeCombo.getValue();
                addNodeBtn.setEnabled(false);
                start(nodeType);
            }
        });

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(false);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                track = false;
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);
    }

    @Override
    protected void close() {
        super.close();
        track = false;
    }

    private void start(final NodeType nodeType) {
        showProgress();
        MongoUI.getExecutor().execute(new Runnable() {

            public void run() {
                UUID operationID = MongoUI.getMongoManager().addNode(config.getClusterName(), nodeType);
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
