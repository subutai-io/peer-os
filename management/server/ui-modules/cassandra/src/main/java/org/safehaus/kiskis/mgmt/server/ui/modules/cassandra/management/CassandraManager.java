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
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author dilshat
 */
public class CassandraManager {

    private final VerticalLayout contentRoot;
    CassandraTable cassandraTable;

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
        Button getClustersBtn = new Button("Get clusters");
        getClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraTable.refreshDatasource();
            }
        });
        buttons.addComponent(getClustersBtn);
        buttons.addComponent(new Button("Apply Changes"));

        content.addComponent(buttons);

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);
        cassandraTable = new CassandraTable();
        content.addComponent(cassandraTable);

    }

    private void updateTableData() {

    }

    public Component getContent() {
        return contentRoot;
    }

}
