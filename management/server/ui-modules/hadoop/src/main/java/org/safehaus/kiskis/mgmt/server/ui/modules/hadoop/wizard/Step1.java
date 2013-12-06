/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bahadyr
 */
public class Step1 extends Panel {

    private HadoopWizard parent;
    private List<Agent> lxcAgent;

    public Step1(final HadoopWizard hadoopWizard) {
        this.parent = hadoopWizard;

        lxcAgent = getLxcAgents();

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

        final TextField textFieldClusterName = new TextField("Enter your cluster name");
        textFieldClusterName.setInputPrompt("Cluster name");
        textFieldClusterName.setRequired(true);
        textFieldClusterName.setRequiredError("Must have a name");
        verticalLayoutForm.addComponent(textFieldClusterName);

        Label labelNameNode = new Label("Choose the host that will run Name Node:");
        verticalLayoutForm.addComponent(labelNameNode);

        ComboBox comboBoxNameNode = new ComboBox("Name Node");
        comboBoxNameNode.setMultiSelect(false);
        for(Agent agent : lxcAgent){
            comboBoxNameNode.addItem(agent.getUuid());
        }
        comboBoxNameNode.addListener(new Property.ValueChangeListener(){
            @Override
            public void valueChange(Property.ValueChangeEvent event){
                System.out.println(event.getProperty());
            }
        });
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
                createTask();
                parent.setClusterName(textFieldClusterName.getValue().toString());
                hadoopWizard.showNext();

            }
        });

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(next);

        addComponent(verticalLayout);
    }

    private void createTask() {
        Task clusterTask = new Task();
        clusterTask.setTaskStatus(TaskStatus.NEW);
        clusterTask.setDescription("Setup Hadoop cluster");

        parent.setTask(clusterTask);
    }

    private List<Agent> getLxcAgents() {
        if (AppData.getSelectedAgentList() != null) {
            List<Agent> list =  new ArrayList<Agent>();
            for(Agent agent : AppData.getSelectedAgentList()) {
                if(agent.isIsLXC()){
                    list.add(agent);
                }
            }

            if(list.size() == 0){
                getWindow().showNotification("Select lxc agents at first");
                parent.setStep(-1);
                parent.showNext();
            } else {
                return list;
            }
        }

        return null;
    }

}
