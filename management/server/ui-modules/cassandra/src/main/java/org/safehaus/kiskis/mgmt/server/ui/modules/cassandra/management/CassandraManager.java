/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author dilshat
 */
public class CassandraManager {

    private final VerticalLayout contentRoot;

    public CassandraManager() {
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

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(new Button("Get clusters"));
        buttons.addComponent(new Button("Apply Changes"));

        content.addComponent(buttons);

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);

        Table clustersTable = new Table("Cluster");
        clustersTable.addContainerProperty("Host", String.class, null);
        clustersTable.addContainerProperty("Start", Button.class, null);
        clustersTable.addContainerProperty("Stop", Button.class, null);

        clustersTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        clustersTable.setHeight(100, Sizeable.UNITS_PIXELS);

        clustersTable.setPageLength(10);
        clustersTable.setSelectable(true);
        clustersTable.setImmediate(true);

        //sample data for UI test=============================
        clustersTable.addItem(new Object[]{
            "Router-1", new Button("Start"), new Button("Stop")}, new Integer(1));
        clustersTable.addItem(new Object[]{
            "Router-2", new Button("Start"), new Button("Stop")}, new Integer(2));
        //====================================================

        content.addComponent(clustersTable);

    }

    public Component getContent() {
        return contentRoot;
    }

}
