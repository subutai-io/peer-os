/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
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
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);        

        Label confirmationLbl = new Label("<strong>Please verify the installation configuration,"
                + "<br/>you can change it by clicking on Back button</strong>");
        confirmationLbl.setContentMode(Label.CONTENT_XHTML);

        TreeTable summaryTable = new TreeTable();
        summaryTable.addContainerProperty("Installation configuration", String.class, "");
        summaryTable.addItem(new Object[]{"Configuration servers"}, "cfg");
        for (Agent configServer : wizard.getConfig().getConfigServers()) {
            summaryTable.addItem(new Object[]{configServer.getHostname()}, "cfg_" + configServer.getHostname());
            summaryTable.setParent("cfg_" + configServer.getHostname(), "cfg");
            summaryTable.setChildrenAllowed("cfg_" + configServer.getHostname(), false);
        }
        summaryTable.addItem(new Object[]{"Routers servers"}, "rtr");
        for (Agent routerServer : wizard.getConfig().getRouterServers()) {
            summaryTable.addItem(new Object[]{routerServer.getHostname()}, "rtr_" + routerServer.getHostname());
            summaryTable.setParent("rtr_" + routerServer.getHostname(), "rtr");
            summaryTable.setChildrenAllowed("rtr_" + routerServer.getHostname(), false);
        }
        summaryTable.addItem(new Object[]{"Data nodes"}, "data");
        for (Agent dataNode : wizard.getConfig().getDataNodes()) {
            summaryTable.addItem(new Object[]{dataNode.getHostname()}, "data_" + dataNode.getHostname());
            summaryTable.setParent("data_" + dataNode.getHostname(), "data");
            summaryTable.setChildrenAllowed("data_" + dataNode.getHostname(), false);
        }
        summaryTable.setCollapsed("cfg", false);
        summaryTable.setCollapsed("rtr", false);
        summaryTable.setCollapsed("data", false);
        
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
        buttons.addComponent(back);
        buttons.addComponent(install);

        content.addComponent(confirmationLbl);

        content.addComponent(summaryTable);

        content.addComponent(buttons);

        addComponent(content);

    }

}
