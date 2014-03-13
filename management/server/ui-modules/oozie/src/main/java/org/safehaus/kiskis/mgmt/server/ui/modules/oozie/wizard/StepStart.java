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

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClustersTable;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dilshat
 */
public class StepStart extends Panel {

    private ClustersTable table;
    private Item selectedItem;
//    private OozieDAO oozieDAO;

    public StepStart(final Wizard wizard) {
//        this.oozieDAO = oozieDAO;
        setSizeFull();
        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Oozie Installation Wizard!</h2><br/>"
                + "Please click Start button to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label(
                String.format("<img src='http://%s:%s/oozie_logo.png' width='150px'/>", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT));
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
                    HadoopClusterInfo cluster = wizard.getOozieDAO().getHadoopClusterInfo(clusterName);

                    Set<Agent> taskTrackers = new HashSet<Agent>();
                    taskTrackers.add(cluster.getJobTracker());

                    Set<Agent> nodes = new HashSet<Agent>(cluster.getDataNodes());
                    nodes.add(cluster.getNameNode());
                    nodes.add(cluster.getSecondaryNameNode());
                    nodes.addAll(cluster.getTaskTrackers());

                    wizard.getConfig().reset();
                    wizard.getConfig().setServer(taskTrackers.iterator().next());
                    wizard.getConfig().setClients(nodes);
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
            }
        });

        hl.addComponent(refresh);
        hl.addComponent(next);

        gridLayout.addComponent(hl, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(refresh, Alignment.BOTTOM_RIGHT);
        addComponent(gridLayout);
        table = getTable();
        addComponent(table);
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
