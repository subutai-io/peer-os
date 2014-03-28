/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.tracker;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.ProductOperationState;
import org.safehaus.kiskis.mgmt.shared.protocol.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class Tracker {

    private final VerticalLayout contentRoot;
    private final Table operationsTable;
    private final TextArea outputTxtArea;
    private final String okIconSource = "icons/16/ok.png";
    private final String errorIconSource = "icons/16/cancel.png";
    private final String loadIconSource = "../base/common/img/loading-indicator.gif";
    private final PopupDateField fromDate, toDate;
    private volatile UUID trackID;
    private volatile boolean track = false;
    private List<ProductOperationView> currentOperations = new ArrayList<ProductOperationView>();

    public Tracker() {
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setMargin(true);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setSpacing(true);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        fromDate = new PopupDateField("From", cal.getTime());
        toDate = new PopupDateField("To", new Date());

        fromDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fromDate.setInvalidAllowed(false);
        fromDate.setInvalidCommitted(false);
        toDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
        toDate.setInvalidAllowed(false);
        toDate.setInvalidCommitted(false);

        filterLayout.addComponent(fromDate);
        filterLayout.addComponent(toDate);

        operationsTable = createTableTemplate("Operations", 250);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows(20);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(filterLayout);
        content.addComponent(operationsTable);
        content.addComponent(outputTxtArea);
        content.setComponentAlignment(operationsTable, Alignment.TOP_CENTER);
        content.setComponentAlignment(outputTxtArea, Alignment.TOP_CENTER);

    }

    public Component getContent() {
        return contentRoot;
    }

    public void setTrackId(UUID trackID) {
        this.trackID = trackID;
    }

    public void startTracking() {
        if (!track) {
            track = true;

            MongoUI.getExecutor().execute(new Runnable() {

                public void run() {
                    while (track) {
                        populateOperations();
                        populateLogs();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            break;
                        }

                    }
                }
            });
        }
    }

    public void stopTracking() {
        track = false;
    }

    private void populateLogs() {
        if (trackID != null) {
            ProductOperationView po = MongoUI.getMongoManager().getProductOperationView(trackID);
            if (po != null) {
                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                if (po.getState() != ProductOperationState.RUNNING) {
                    trackID = null;
                }
            } else {
                setOutput("Product operation not found. Check logs");
            }
        }
    }

    private void populateOperations() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        List<ProductOperationView> operations = MongoUI.getDbManager().getProductOperations(
                Config.PRODUCT_KEY, (Date) fromDate.getValue(), (Date) toDate.getValue(), 100);
        IndexedContainer container = (IndexedContainer) operationsTable.getContainerDataSource();
        currentOperations.removeAll(operations);

        for (ProductOperationView po : currentOperations) {
            container.removeItem(po.getId());
        }
        boolean sortNeeded = false;
        for (final ProductOperationView po : operations) {
            Embedded progressIcon;
            if (po.getState() == ProductOperationState.RUNNING) {
                progressIcon = new Embedded("", new ThemeResource(loadIconSource));
            } else if (po.getState() == ProductOperationState.FAILED) {
                progressIcon = new Embedded("", new ThemeResource(errorIconSource));
            } else {
                progressIcon = new Embedded("", new ThemeResource(okIconSource));
            }

            Item item = container.getItem(po.getId());
            if (item == null) {
                final Button trackLogsBtn = new Button("View logs");
                trackLogsBtn.addListener(new Button.ClickListener() {

                    public void buttonClick(Button.ClickEvent event) {
                        setTrackId(po.getId());
                    }
                });

                item = container.addItem(po.getId());
                item.getItemProperty("Date").setValue(po.getCreateDate());
                item.getItemProperty("Operation").setValue(po.getDescription());
                item.getItemProperty("Check").setValue(trackLogsBtn);
                item.getItemProperty("Status").setValue(progressIcon);

                sortNeeded = true;
            } else {
                if (!((Embedded) item.getItemProperty("Status").getValue()).getSource().equals(progressIcon.getSource())) {
                    item.getItemProperty("Status").setValue(progressIcon);
                }
            }

        }

        if (sortNeeded) {
            Object[] properties = {"Date"};
            boolean[] ordering = {false};
            operationsTable.sort(properties, ordering);
        }

        currentOperations = operations;
    }

    private Table createTableTemplate(String caption, int height) {
        Table table = new Table(caption);
        table.setContainerDataSource(new IndexedContainer());
        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Operation", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(height, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
