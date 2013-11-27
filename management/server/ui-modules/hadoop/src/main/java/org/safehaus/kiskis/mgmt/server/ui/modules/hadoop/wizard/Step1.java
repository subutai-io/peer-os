/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

/**
 * @author bahadyr
 */
public class Step1 extends Panel {

    public Step1(final HadoopWizard hadoopWizard) {
        setCaption("Welcome to Hadoop Cluster Installation");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 15);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Master Configurations</strong></font><br>"
                + " 2) Slave Configurations<br>");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 1, 14);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayout.setSpacing(true);

        TextField textFieldClusterName = new TextField("Enter your cluster name");
        textFieldClusterName.setInputPrompt("Cluster name");
        verticalLayoutForm.addComponent(textFieldClusterName);

        Label labelNameNode = new Label("Choose the host that will run Name Node:");
        verticalLayoutForm.addComponent(labelNameNode);

        ComboBox comboBoxNameNode = new ComboBox("Name Node");
        // add items
        verticalLayoutForm.addComponent(comboBoxNameNode);

        Label labelJobTracker = new Label("Choose the host that will run Job Tracker:");
        verticalLayoutForm.addComponent(labelJobTracker);

        ComboBox comboBoxJobTracker = new ComboBox("Job Tracker");
        // add items
        verticalLayoutForm.addComponent(comboBoxJobTracker);

        Label labelSecondaryNameNode = new Label("Choose the host that will run Secondary Name Node:");
        verticalLayoutForm.addComponent(labelSecondaryNameNode);

        ComboBox comboBoxSecondaryNameNode = new ComboBox("Secondary Name Node");
        // add items
        verticalLayoutForm.addComponent(comboBoxSecondaryNameNode);

        Label labelReplicationFactor = new Label("Specify the replication factor:");
        verticalLayoutForm.addComponent(labelReplicationFactor);

        ComboBox comboBoxReplicationFactor = new ComboBox("Dfs Replication Factor");
        // add items
        verticalLayoutForm.addComponent(comboBoxReplicationFactor);

        grid.addComponent(verticalLayoutForm, 2, 0, 5, 14);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                hadoopWizard.showNext();
            }
        });

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(next);

        addComponent(verticalLayout);
    }

}
