/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class Step4 extends Panel {

    private static final Logger LOG = Logger.getLogger(Step4.class.getName());

    public Step4(final Wizard wizard) {

        VerticalLayout content = new VerticalLayout();

        Label confirmationLbl = new Label("<strong>Please verify the installation configuration,<br/>you can change it by clicking on Back buton</strong>");
        confirmationLbl.setContentMode(Label.CONTENT_XHTML);

        TreeTable summaryTable = new TreeTable();
        summaryTable.addContainerProperty("Installation configuration", String.class, "");
        Object configServers = summaryTable.addItem("Configuration servers");
        for (Agent configServer : wizard.getConfig().getConfigServers()) {
            Object item = summaryTable.addItem(configServer.getHostname());
            summaryTable.setParent(item, configServers);
            summaryTable.setChildrenAllowed(item, false);
        }
        Object routerServers = summaryTable.addItem("Routers servers");
        for (Agent routerServer : wizard.getConfig().getRouterServers()) {
            Object item = summaryTable.addItem(routerServer.getHostname());
            summaryTable.setParent(item, routerServers);
            summaryTable.setChildrenAllowed(item, false);
        }
        Object dataNodes = summaryTable.addItem("Data nodes");
        for (Agent dataNode : wizard.getConfig().getDataNodes()) {
            Object item = summaryTable.addItem(dataNode.getHostname());
            summaryTable.setParent(item, dataNodes);
            summaryTable.setChildrenAllowed(item, false);
        }
        summaryTable.setCollapsed(configServers, false);
        summaryTable.setCollapsed(routerServers, false);
        summaryTable.setCollapsed(dataNodes, false);

        Button install = new Button("Install");
        install.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.next();
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(install);
        buttons.addComponent(back);

        content.addComponent(confirmationLbl);

        content.addComponent(summaryTable);

        content.addComponent(buttons);

        addComponent(content);

    }

}
