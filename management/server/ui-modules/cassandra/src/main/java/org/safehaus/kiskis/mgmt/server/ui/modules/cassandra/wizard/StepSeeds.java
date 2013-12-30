/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
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
public class StepSeeds extends Panel {

    public StepSeeds(final CassandraWizard wizard) {
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

        final TextField domainNameTxtFld = new TextField("Domain name");
        domainNameTxtFld.setInputPrompt("Domain name");
        domainNameTxtFld.setRequired(true);
        domainNameTxtFld.setMaxLength(20);
        verticalLayoutForm.addComponent(domainNameTxtFld);

        final TextField clusterNameTxtFld = new TextField("Cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);
        verticalLayoutForm.addComponent(clusterNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as seeds");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(configServersLabel);

        final TwinColSelect seedsColSel = new TwinColSelect("", new ArrayList<Agent>());
        seedsColSel.setItemCaptionPropertyId("hostname");
        seedsColSel.setRows(7);
        seedsColSel.setNullSelectionAllowed(true);
        seedsColSel.setMultiSelect(true);
        seedsColSel.setImmediate(true);
        seedsColSel.setLeftColumnCaption("Available Nodes");
        seedsColSel.setRightColumnCaption("Seeds");
        seedsColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        seedsColSel.setRequired(true);

        verticalLayoutForm.addComponent(seedsColSel);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.getConfig().setDomainName(domainNameTxtFld.getValue().toString().trim());
                wizard.getConfig().setClusterName(clusterNameTxtFld.getValue().toString().trim());
                wizard.getConfig().setSeeds((Set<Agent>) seedsColSel.getValue());

                if (wizard.getConfig().getDomainName().isEmpty()) {
                    show("Please provide domain name");
                } else if (wizard.getConfig().getClusterName().isEmpty()) {
                    show("Please provide cluster name");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getSeeds())) {
                    show("Please add seeds servers");
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

        seedsColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, wizard.getConfig().getSelectedAgents()));

        //set values if this is a second visit
        domainNameTxtFld.setValue(wizard.getConfig().getDomainName());
        clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());
        seedsColSel.setValue(Util.retainValues(wizard.getConfig().getSeeds(), wizard.getConfig().getSelectedAgents()));
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
