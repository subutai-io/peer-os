/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management;

import com.vaadin.ui.*;

/**
 *
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout content;
    private final HBaseTable table;

    public Manager() {

        content = new VerticalLayout();
        content.setMargin(true);

        HorizontalLayout buttons = new HorizontalLayout();

        Label clusterNameLabel = new Label("Select the cluster");

        table = new HBaseTable();
        Button getClustersBtn = new Button("Get clusters");
        getClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                table.refreshDatasource();
            }
        });
        buttons.addComponent(getClustersBtn);

        content.addComponent(clusterNameLabel);
        content.addComponent(buttons);
        content.addComponent(table);

    }

    public Component getContent() {
        return content;
    }

}
