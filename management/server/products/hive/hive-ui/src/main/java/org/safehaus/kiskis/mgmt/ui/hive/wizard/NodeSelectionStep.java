package org.safehaus.kiskis.mgmt.ui.hive.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.hive.HiveUI;

public class NodeSelectionStep extends Panel {

    private final ComboBox hadoopClusters;
    private final ComboBox serverNode;
    private final TwinColSelect select;

    public NodeSelectionStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        hadoopClusters = new ComboBox("Hadoop cluster");
        serverNode = makeServerNodeComboBox(wizard);
        select = makeClientNodeSelector(wizard);

        hadoopClusters.setMultiSelect(false);
        hadoopClusters.setImmediate(true);
        hadoopClusters.setTextInputAllowed(false);
        hadoopClusters.setRequired(true);
        hadoopClusters.setNullSelectionAllowed(false);
        hadoopClusters.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent e) {
                serverNode.removeAllItems();
                select.setValue(null);
                if(e.getProperty().getValue() != null) {
                    HadoopClusterInfo hadoopInfo = (HadoopClusterInfo)e.getProperty().getValue();
                    for(Agent a : hadoopInfo.getAllAgents()) {
                        serverNode.addItem(a);
                        serverNode.setItemCaption(a, a.getHostname());
                    }
                    select.setContainerDataSource(new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllAgents())
                    );
                    // do select if values exist
                    if(wizard.getConfig().getServer() != null)
                        serverNode.setValue(wizard.getConfig().getServer());
                    if(!Util.isCollectionEmpty(wizard.getConfig().getClients()))
                        select.setValue(wizard.getConfig().getClients());

                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                }
            }
        });

        List<HadoopClusterInfo> clusters = HiveUI.getDbManager().getInfo(
                HadoopClusterInfo.SOURCE, HadoopClusterInfo.class);
        if(clusters.size() > 0) {
            for(HadoopClusterInfo hci : clusters) {
                hadoopClusters.addItem(hci);
                hadoopClusters.setItemCaption(hci, hci.getClusterName());
            }
        }

        HadoopClusterInfo info = HiveUI.getDbManager().getInfo(
                HadoopClusterInfo.SOURCE, wizard.getConfig().getClusterName(),
                HadoopClusterInfo.class);
        if(info != null) hadoopClusters.setValue(info);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if(Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Select Hadoop cluster");
                } else if(wizard.getConfig().getServer() == null) {
                    show("Select server node");
                } else if(Util.isCollectionEmpty(wizard.getConfig().getClients())) {
                    show("Select client nodes");
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

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label("Please, specify installation settings"));
        layout.addComponent(content);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);

        content.addComponent(hadoopClusters);
        content.addComponent(serverNode);
        content.addComponent(select);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private TwinColSelect makeClientNodeSelector(final Wizard wizard) {
        TwinColSelect tcs = new TwinColSelect("Client nodes");
        tcs.setItemCaptionPropertyId("hostname");
        tcs.setRows(7);
        tcs.setMultiSelect(true);
        tcs.setImmediate(true);
        tcs.setLeftColumnCaption("Available Nodes");
        tcs.setRightColumnCaption("Selected Nodes");
        tcs.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        tcs.setRequired(true);
        if(!Util.isCollectionEmpty(wizard.getConfig().getClients())) {
            tcs.setValue(wizard.getConfig().getClients());
        }
        tcs.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    Set<Agent> clients = new HashSet();
                    clients.addAll((Collection)event.getProperty().getValue());
                    wizard.getConfig().setClients(clients);
                }
            }
        });
        return tcs;
    }

    private ComboBox makeServerNodeComboBox(final Wizard wizard) {
        ComboBox cb = new ComboBox("Server node");
        cb.setMultiSelect(false);
        cb.setImmediate(true);
        cb.setTextInputAllowed(false);
        cb.setRequired(true);
        cb.setNullSelectionAllowed(false);
        cb.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                Agent serverNode = (Agent)event.getProperty().getValue();
                wizard.getConfig().setServer(serverNode);

                HadoopClusterInfo hci = (HadoopClusterInfo)hadoopClusters.getValue();
                select.removeAllItems();
                for(Agent a : hci.getAllAgents()) {
                    if(a.equals(serverNode)) continue;
                    select.addItem(a);
                    select.setItemCaption(a, a.getHostname());
                }
            }
        });
        if(wizard.getConfig().getServer() != null)
            cb.setValue(wizard.getConfig().getServer());
        return cb;
    }

}
