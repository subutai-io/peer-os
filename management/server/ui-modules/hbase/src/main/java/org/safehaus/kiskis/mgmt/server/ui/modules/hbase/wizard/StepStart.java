/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClustersTable;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class StepStart extends Panel {

    private ClustersTable table;
    private Item selectedItem;

    public StepStart(final Wizard wizard) {
        setSizeFull();
        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to HBase Installation Wizard!</h2><br/>"
                + "Please click Start button to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label(
                String.format("<img src='http://%s:%s/hbase_logo.png' width='150px'/>", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT));
        logoImg.setContentMode(Label.CONTENT_XHTML);
        logoImg.setHeight(150, Sizeable.UNITS_PIXELS);
        logoImg.setWidth(220, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Start");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(next, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Agent> selectedAgents = MgmtApplication.getSelectedAgents();
                if (Util.isCollectionEmpty(selectedAgents)) {
                    show("Select nodes in the tree on the left first");
                } else {
                    wizard.getConfig().reset();
                    wizard.getConfig().setAgents(selectedAgents);
                    wizard.next();
                }

                /*
                 if (selectedItem != null) {
                 UUID uid = (UUID) selectedItem.getItemProperty(HadoopClusterInfo.UUID_LABEL).getValue();
                 HadoopClusterInfo cluster = HadoopDAO.getHadoopClusterInfo(uid);

                 Set<UUID> dataNodes = new HashSet<UUID>(cluster.getDataNodes());
                 Set<UUID> taskTrackers = new HashSet<UUID>(cluster.getTaskTrackers());
                 dataNodes.addAll(taskTrackers);
                 dataNodes.add(cluster.getJobTracker());
                 dataNodes.add(cluster.getNameNode());
                 dataNodes.add(cluster.getSecondaryNameNode());

                 Set<Agent> set = HBaseDAO.getAgents(dataNodes);

                 wizard.getConfig().reset();
                 wizard.getConfig().setAgents(set);
                 wizard.next();
                 } else {
                 show("Please select Hadoop cluster first");
                 }
                 */
            }
        });
        addComponent(gridLayout);
        addComponent(getTable());
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
