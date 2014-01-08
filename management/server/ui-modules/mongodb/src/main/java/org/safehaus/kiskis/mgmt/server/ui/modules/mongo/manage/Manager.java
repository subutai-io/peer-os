/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class Manager implements ResponseListener {

    private final VerticalLayout contentRoot;
    private final PersistenceInterface persistenceManager;

    public Manager() {
        persistenceManager = ServiceLocator.getService(PersistenceInterface.class);

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);
        //tables go here
        final Table routersTable = new Table("Query Routers");

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);

        HorizontalLayout hl = new HorizontalLayout();

        final ComboBox clusterCombo = new ComboBox();
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof MongoClusterInfo) {
                    MongoClusterInfo clusterInfo = (MongoClusterInfo) event.getProperty().getValue();
                    routersTable.removeAllItems();
                    for (UUID routerUUID : clusterInfo.getRouters()) {
                        Agent router = persistenceManager.getAgent(routerUUID);
                        routersTable.addItem(new Object[]{
                            router.getHostname(),
                            new Button("Check"),
                            new Button("Start"),
                            new Button("Stop"),
                            new Button("Destroy")},
                                router);
                    }
                }
            }
        });

        hl.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Get clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<MongoClusterInfo> clusters = persistenceManager.getMongoClustersInfo();
                clusterCombo.removeAllItems();
                for (MongoClusterInfo clusterInfo : clusters) {
                    clusterCombo.addItem(clusterInfo);
                    clusterCombo.setItemCaption(clusterInfo, clusterInfo.getClusterName());
                }
            }
        });

        hl.addComponent(refreshClustersBtn);

        content.addComponent(hl);

        routersTable.addContainerProperty("Host", String.class, null);
        routersTable.addContainerProperty("Check", Button.class, null);
        routersTable.addContainerProperty("Start", Button.class, null);
        routersTable.addContainerProperty("Stop", Button.class, null);
        routersTable.addContainerProperty("Destroy", Button.class, null);

        routersTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        routersTable.setHeight(100, Sizeable.UNITS_PIXELS);

        routersTable.setPageLength(10);
        routersTable.setSelectable(true);
        routersTable.setImmediate(true);

        content.addComponent(routersTable);

        Table configServersTable = new Table("Config Servers");
        configServersTable.addContainerProperty("Host", String.class, null);
        configServersTable.addContainerProperty("Start", Button.class, null);
        configServersTable.addContainerProperty("Stop", Button.class, null);

        configServersTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        configServersTable.setHeight(100, Sizeable.UNITS_PIXELS);

        configServersTable.setPageLength(10);
        configServersTable.setSelectable(true);
        configServersTable.setImmediate(true);

        //sample data for UI test=============================
        configServersTable.addItem(new Object[]{
            "Config-1", new Button("Start"), new Button("Stop")}, new Integer(1));
        configServersTable.addItem(new Object[]{
            "Config-2", new Button("Start"), new Button("Stop")}, new Integer(2));
        //====================================================

        content.addComponent(configServersTable);

        Label replicaNameLabel = new Label("Replica: rs0");
        content.addComponent(replicaNameLabel);

        Table shardsTable = new Table("Shards");
        shardsTable.addContainerProperty("Host", String.class, null);
        shardsTable.addContainerProperty("Start", Button.class, null);
        shardsTable.addContainerProperty("Stop", Button.class, null);

        shardsTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        shardsTable.setHeight(100, Sizeable.UNITS_PIXELS);

        shardsTable.setPageLength(10);
        shardsTable.setSelectable(true);
        shardsTable.setImmediate(true);

        //sample data for UI test=============================
        shardsTable.addItem(new Object[]{
            "Shard-1", new Button("Start"), new Button("Stop")}, new Integer(1));
        shardsTable.addItem(new Object[]{
            "Shard-2", new Button("Start"), new Button("Stop")}, new Integer(2));
        //====================================================

        content.addComponent(shardsTable);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(new Button("Cancel"));
        buttons.addComponent(new Button("Apply Changes"));

        content.addComponent(buttons);

    }

    public Component getContent() {
        return contentRoot;
    }

    @Override
    public void onResponse(Response response) {
    }

    @Override
    public String getSource() {
        return MongoModule.MODULE_NAME;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

}
