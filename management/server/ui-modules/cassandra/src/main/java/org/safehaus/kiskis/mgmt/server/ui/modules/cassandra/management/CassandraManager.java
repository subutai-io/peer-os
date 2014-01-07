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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CassandraManager {

    private final VerticalLayout contentRoot;
//    private final TextArea terminal;
    private final CassandraTable cassandraTable;

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

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);
//        terminal = new TextArea();
//        terminal.setRows(10);
//        terminal.setColumns(60);

        cassandraTable = new CassandraTable();
        Button getClustersBtn = new Button("Get clusters");
        getClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraTable.refreshDatasource();
            }
        });
        buttons.addComponent(getClustersBtn);

        content.addComponent(buttons);
        content.addComponent(cassandraTable);
//        content.addComponent(terminal);

    }

    public Component getContent() {
        return contentRoot;
    }

    public void setOutput(Response response) {
        cassandraTable.onResponse(response);
    }

}
