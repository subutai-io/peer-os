/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class StepSetConfig extends Panel {

    public StepSetConfig(final Wizard wizard) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as NameNode");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(configServersLabel);

        final Label server = new Label("Server");
        verticalLayoutForm.addComponent(server);

        final TwinColSelect selectClients = new TwinColSelect("", new ArrayList<Agent>());
        selectClients.setItemCaptionPropertyId("hostname");
        selectClients.setRows(7);
        selectClients.setNullSelectionAllowed(true);
        selectClients.setMultiSelect(true);
        selectClients.setImmediate(true);
        selectClients.setLeftColumnCaption("Other nodes");
        selectClients.setRightColumnCaption("Client nodes");
        selectClients.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        selectClients.setRequired(true);

        verticalLayoutForm.addComponent(selectClients);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.getConfig().setClients((Set<Agent>) selectClients.getValue());
//
                if (Util.isCollectionEmpty(wizard.getConfig().getClients())) {
                    show("Please select servers to install server and clients");
                } else {
                    wizard.next();
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

        selectClients.setContainerDataSource(new BeanItemContainer<Agent>(Agent.class, wizard.getConfig().getClients()));

        //set values if this is a second visit
        server.setValue(wizard.getConfig().getServer().getHostname());
        selectClients.setValue(wizard.getConfig().getClients());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
