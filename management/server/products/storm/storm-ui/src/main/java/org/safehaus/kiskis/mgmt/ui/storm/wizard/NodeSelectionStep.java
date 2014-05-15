package org.safehaus.kiskis.mgmt.ui.storm.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.storm.StormUI;

public class NodeSelectionStep extends Panel {

    private final ComboBox zkClustersCombo;
    private final ComboBox masterNodeCombo;
    private final TwinColSelect select;

    public NodeSelectionStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        zkClustersCombo = new ComboBox("Zookeeper cluster");
        masterNodeCombo = makeMasterNodeComboBox(wizard);
        select = makeClientNodeSelector(wizard);

        zkClustersCombo.setMultiSelect(false);
        zkClustersCombo.setImmediate(true);
        zkClustersCombo.setTextInputAllowed(false);
        zkClustersCombo.setRequired(true);
        zkClustersCombo.setNullSelectionAllowed(false);
        zkClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent e) {
                masterNodeCombo.removeAllItems();
                select.setValue(null);
                if(e.getProperty().getValue() != null) {
                    org.safehaus.kiskis.mgmt.api.zookeeper.Config zk;
                    zk = (org.safehaus.kiskis.mgmt.api.zookeeper.Config)e.getProperty().getValue();
                    for(Agent a : zk.getNodes()) {
                        masterNodeCombo.addItem(a);
                        masterNodeCombo.setItemCaption(a, a.getHostname());
                    }
                    select.setContainerDataSource(new BeanItemContainer<Agent>(
                            Agent.class, zk.getNodes())
                    );
                    // do select if values exist
                    if(wizard.getConfig().getNimbus() != null)
                        masterNodeCombo.setValue(wizard.getConfig().getNimbus());
                    if(!Util.isCollectionEmpty(wizard.getConfig().getSupervisors()))
                        select.setValue(wizard.getConfig().getSupervisors());

                    wizard.getConfig().setClusterName(zk.getClusterName());
                }
            }
        });

        List<org.safehaus.kiskis.mgmt.api.zookeeper.Config> zk_list
                = StormUI.getZookeeper().getClusters();
        if(zk_list.size() > 0)
            for(org.safehaus.kiskis.mgmt.api.zookeeper.Config zkc : zk_list) {
                zkClustersCombo.addItem(zkc);
                zkClustersCombo.setItemCaption(zkc, zkc.getClusterName());
                if(zkc.getClusterName().equals(wizard.getConfig().getClusterName()))
                    zkClustersCombo.setValue(zkc);
            }
        // set selected values
        if(wizard.getConfig().getNimbus() != null)
            masterNodeCombo.setValue(wizard.getConfig().getNimbus());
        select.setValue(wizard.getConfig().getSupervisors());

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if(Util.isStringEmpty(wizard.getConfig().getClusterName()))
                    show("Select Zookeeper cluster");
                else if(wizard.getConfig().getNimbus() == null)
                    show("Select master node");
                else if(Util.isCollectionEmpty(wizard.getConfig().getSupervisors()))
                    show("Select worker nodes");
                else
                    wizard.next();
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

        content.addComponent(zkClustersCombo);
        content.addComponent(masterNodeCombo);
        content.addComponent(select);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private TwinColSelect makeClientNodeSelector(final Wizard wizard) {
        TwinColSelect tcs = new TwinColSelect("Worker nodes");
        tcs.setItemCaptionPropertyId("hostname");
        tcs.setRows(7);
        tcs.setMultiSelect(true);
        tcs.setImmediate(true);
        tcs.setLeftColumnCaption("Available Nodes");
        tcs.setRightColumnCaption("Selected Nodes");
        tcs.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        tcs.setRequired(true);
        tcs.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    Set<Agent> clients = new HashSet();
                    clients.addAll((Collection)event.getProperty().getValue());
                    wizard.getConfig().setSupervisors(clients);
                }
            }
        });
        return tcs;
    }

    private ComboBox makeMasterNodeComboBox(final Wizard wizard) {
        ComboBox cb = new ComboBox("Master node");
        cb.setMultiSelect(false);
        cb.setImmediate(true);
        cb.setTextInputAllowed(false);
        cb.setRequired(true);
        cb.setNullSelectionAllowed(false);
        cb.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                Agent serverNode = (Agent)event.getProperty().getValue();
                wizard.getConfig().setNimbus(serverNode);

                org.safehaus.kiskis.mgmt.api.zookeeper.Config zk;
                zk = (org.safehaus.kiskis.mgmt.api.zookeeper.Config)zkClustersCombo.getValue();
                select.removeAllItems();
                for(Agent a : zk.getNodes()) {
                    if(a.equals(serverNode)) continue;
                    select.addItem(a);
                    select.setItemCaption(a, a.getHostname());
                }
            }
        });
        return cb;
    }

}
