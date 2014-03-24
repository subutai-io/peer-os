/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClustersTable;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dilshat
 */
public class StepStart extends Panel {

    private ClustersTable table;
    private Item selectedItem;
    HorizontalLayout hlcluster;

    public StepStart(final Wizard wizard) {
        setSizeFull();
        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to HBase Installation Wizard!</h2><br/>"
                + "Please click Start button to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/hbase.png"));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(150, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(220, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        HorizontalLayout hl = new HorizontalLayout();

        Button next = new Button("Start");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                Set<Agent> selectedAgents = MgmtApplication.getSelectedAgents();
//                if (Util.isCollectionEmpty(selectedAgents)) {
//                    show("Select nodes in the tree on the left first");
//                } else {
//                    wizard.getConfig().reset();
//                    wizard.getConfig().setAgents(selectedAgents);
//                    wizard.next();
//                }

                if (selectedItem != null) {
                    String clusterName = (String) selectedItem.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue();
                    HadoopClusterInfo cluster = HBaseDAO.getHadoopClusterInfo(clusterName);

                    Set<Agent> dataNodes = new HashSet<Agent>(cluster.getDataNodes());
                    Set<Agent> taskTrackers = new HashSet<Agent>(cluster.getTaskTrackers());
                    dataNodes.addAll(taskTrackers);
                    dataNodes.add(cluster.getJobTracker());
                    dataNodes.add(cluster.getNameNode());
                    dataNodes.add(cluster.getSecondaryNameNode());

//                    Set<Agent> set = HBaseDAO.getAgents(dataNodes);
                    wizard.getConfig().reset();
                    wizard.getConfig().setAgents(dataNodes);
                    wizard.next();
                } else {
                    show("Please select Hadoop cluster first");
                }

            }
        });

        Button refresh = new Button("Refresh");
        refresh.setWidth(100, Sizeable.UNITS_PIXELS);
        refresh.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                table = getTable();
                table.refreshDataSource();
                hlcluster.removeAllComponents();
                hlcluster.addComponent(table);
            }
        });

        hl.addComponent(refresh);
        hl.addComponent(next);

        gridLayout.addComponent(hl, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(refresh, Alignment.BOTTOM_RIGHT);
        addComponent(gridLayout);
        table = getTable();
        hlcluster = new HorizontalLayout();
        hlcluster.setSizeFull();
        hlcluster.addComponent(table);
        addComponent(hlcluster);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private ClustersTable getTable() {
        table = new ClustersTable();
        table.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                selectedItem = event.getItem();
            }

        });
        return table;
    }

}
