/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class Step2 extends Panel {
    HadoopWizard parent;

    public Step2(final HadoopWizard hadoopWizard) {
        parent = hadoopWizard;
        BeanItemContainer<Agent> agents = new BeanItemContainer<Agent>(Agent.class, parent.getLxcList());

        setCaption("Welcome to Hadoop Cluster Installation");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) Master Configurations<br>"
                + " 2) <font color=\"#f14c1a\"><strong>Slave Configurations</strong></font><br>"
                + " 3) Installation<br>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 0, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        Label label = new Label("<strong>Enter a list of hosts that will run as Data nodes.<br>" +
                "(Provide at least 1)</strong>");
        label.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(label);


        TwinColSelect twinColSelectDataNodes = new TwinColSelect("", agents);
        twinColSelectDataNodes.setItemCaptionPropertyId("hostname");
        twinColSelectDataNodes.setRows(10);
        twinColSelectDataNodes.setNullSelectionAllowed(true);
        twinColSelectDataNodes.setMultiSelect(true);
        twinColSelectDataNodes.setImmediate(true);
        twinColSelectDataNodes.setLeftColumnCaption("Available Nodes");
        twinColSelectDataNodes.setRightColumnCaption("Data Nodes");
        twinColSelectDataNodes.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        twinColSelectDataNodes.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                List<Agent> dataNodes = new ArrayList<Agent>(agentList);
                parent.getHadoopInstallation().setDataNodes(dataNodes);
            }
        });
        verticalLayoutForm.addComponent(twinColSelectDataNodes);

        Label labelTaskTrackerCaption = new Label("<strong>Enter a list of hosts that will run as Task tracker.<br>" +
                "(Provide at least 1)</strong>");
        labelTaskTrackerCaption.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(labelTaskTrackerCaption);

        TwinColSelect twinColSelectTaskTrackers = new TwinColSelect("", agents);
        twinColSelectTaskTrackers.setItemCaptionPropertyId("hostname");
        twinColSelectTaskTrackers.setRows(10);
        twinColSelectTaskTrackers.setNullSelectionAllowed(true);
        twinColSelectTaskTrackers.setMultiSelect(true);
        twinColSelectTaskTrackers.setImmediate(true);
        twinColSelectTaskTrackers.setLeftColumnCaption("Available Nodes");
        twinColSelectTaskTrackers.setRightColumnCaption("Task Trackers");
        twinColSelectTaskTrackers.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        twinColSelectTaskTrackers.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                List<Agent> taskTrackers = new ArrayList<Agent>(agentList);
                parent.getHadoopInstallation().setTaskTrackers(taskTrackers);
            }
        });
        verticalLayoutForm.addComponent(twinColSelectTaskTrackers);

        grid.addComponent(verticalLayoutForm, 1, 0, 5, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.MIDDLE_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parent.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parent.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

}
