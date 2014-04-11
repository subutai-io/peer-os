/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hbase.wizard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;

/**
 * @author dilshat
 */
public class StepSetBackuUpMasters extends Panel {

    public StepSetBackuUpMasters(final Wizard wizard) {
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

        Label configServersLabel = new Label("<strong>Choose hosts that will act as BackUpMaster");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(configServersLabel);

        final TwinColSelect select = new TwinColSelect("", new ArrayList<Agent>());
        select.setItemCaptionPropertyId("hostname");
        select.setRows(7);
        select.setNullSelectionAllowed(false);
        select.setMultiSelect(false);
        select.setImmediate(true);
        select.setLeftColumnCaption("Available Nodes");
        select.setRightColumnCaption("BackUpMaster");
        select.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        select.setRequired(true);

        verticalLayoutForm.addComponent(select);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.getConfig().setBackupMasters((Agent) select.getValue());
                if (wizard.getConfig().getBackupMasters() == null) {
                    show("Please add backup servers");
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
        select.setContainerDataSource(new BeanItemContainer<Agent>(Agent.class, wizard.getConfig().getNodes()));
        select.setValue(wizard.getConfig().getBackupMasters());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
