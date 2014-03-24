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
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClustersTable;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class StepStart extends Panel {

    private static final Logger LOG = Logger.getLogger(StepStart.class.getName());

//    private ClustersTable table;
    private Item selectedItem;
    VerticalLayout vLayout;
    ClustersTable table;
//    private OozieDAO oozieDAO;
    HorizontalLayout hlcluster;

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

        Label logoImg = new Label();
        logoImg.setIcon(new ThemeResource("icons/modules/oozie.png"));
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

                if (selectedItem != null) {
                    String clusterName = (String) selectedItem.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue();

//                    HadoopClusterInfo cluster = wizard.getOozieDAO().getHadoopClusterInfo(clusterName);
                    HadoopClusterInfo hci = table.getHCI(clusterName);
                    if (hci != null) {
                        Set<Agent> taskTrackers = new HashSet<Agent>();
                        taskTrackers.add(hci.getJobTracker());

                        Set<Agent> nodes = new HashSet<Agent>(hci.getDataNodes());
                        nodes.add(hci.getNameNode());
                        nodes.add(hci.getSecondaryNameNode());
                        nodes.addAll(hci.getTaskTrackers());

                        wizard.getConfig().reset();
                        wizard.getConfig().setServer(taskTrackers.iterator().next());
                        wizard.getConfig().setClients(nodes);
                        wizard.next();
                    } else {
                        LOG.log(Level.INFO, "Error while attemtp to get Hadoop cluster info");
                    }
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
        vLayout = new VerticalLayout();
        addComponent(vLayout);
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
