/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui;

import com.google.common.base.Strings;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

import java.util.UUID;

/**
 * @author dilshat
 */
public class ProgressWindow extends Window {

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final UUID trackID;
    private final Tracker tracker;
    private final String source;
    private volatile boolean track = true;

    public ProgressWindow(Tracker tracker, UUID trackID, String source) {
        super("Operation progress");
        setModal(true);
        setClosable(true);
        setWidth(600, ProgressWindow.UNITS_PIXELS);

        this.trackID = trackID;
        this.tracker = tracker;
        this.source = source;

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

        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
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

        showProgress();
        MgmtAppFactory.getExecutor().execute(new Runnable() {

            public void run() {
                while (track) {
                    ProductOperationView po = tracker.getProductOperation(source, trackID);
                    if (po != null) {
                        setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                        if (po.getState() != ProductOperationState.RUNNING) {
                            hideProgress();
                            break;
                        }
                    } else {
                        setOutput("Product operation not found. Check logs");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
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
        if (!Strings.isNullOrEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
