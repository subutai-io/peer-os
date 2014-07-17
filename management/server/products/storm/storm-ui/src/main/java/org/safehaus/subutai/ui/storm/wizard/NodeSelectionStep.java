package org.safehaus.subutai.ui.storm.wizard;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.storm.StormUI;

public class NodeSelectionStep extends Panel {

    private final ComboBox zkClustersCombo;
    private final ComboBox masterNodeCombo;

    public NodeSelectionStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        TextField clusterNameTxt = new TextField("Cluster name");
        clusterNameTxt.setRequired(true);
        clusterNameTxt.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent e) {
                wizard.getConfig().setClusterName(e.getProperty().getValue().toString().trim());
            }
        });
        zkClustersCombo = new ComboBox("Zookeeper cluster");
        masterNodeCombo = makeMasterNodeComboBox(wizard);

        zkClustersCombo.setImmediate(true);
        zkClustersCombo.setTextInputAllowed(false);
        zkClustersCombo.setRequired(true);
        zkClustersCombo.setNullSelectionAllowed(false);
        zkClustersCombo.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent e) {
                masterNodeCombo.removeAllItems();
                if(e.getProperty().getValue() != null) {
                    Config zk;
                    zk = (Config)e.getProperty().getValue();
                    for(Agent a : zk.getNodes()) {
                        masterNodeCombo.addItem(a);
                        masterNodeCombo.setItemCaption(a, a.getHostname());
                    }
                    // do select if values exist
                    if(wizard.getConfig().getNimbus() != null)
                        masterNodeCombo.setValue(wizard.getConfig().getNimbus());

                    wizard.getConfig().setClusterName(zk.getClusterName());
                }
            }
        });

        ComboBox nodesCountCmb = new ComboBox("Number of supervisor nodes",
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        nodesCountCmb.setImmediate(true);
        nodesCountCmb.setRequired(true);
        nodesCountCmb.setTextInputAllowed(false);
        nodesCountCmb.setNullSelectionAllowed(false);
        nodesCountCmb.setValue(wizard.getConfig().getSupervisorsCount());
        nodesCountCmb.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setSupervisorsCount((Integer)event.getProperty().getValue());
            }
        });

        List<Config> zk_list
                = StormUI.getZookeeper().getClusters();
        if(zk_list.size() > 0)
            for(Config zkc : zk_list) {
                zkClustersCombo.addItem(zkc);
                zkClustersCombo.setItemCaption(zkc, zkc.getClusterName());
                if(zkc.getClusterName().equals(wizard.getConfig().getClusterName()))
                    zkClustersCombo.setValue(zkc);
            }
        // set selected values
        if(wizard.getConfig().getClusterName() != null)
            clusterNameTxt.setValue(wizard.getConfig().getClusterName());
        if(wizard.getConfig().getNimbus() != null)
            masterNodeCombo.setValue(wizard.getConfig().getNimbus());
        if(wizard.getConfig().getSupervisorsCount() > 0)
            nodesCountCmb.setValue(wizard.getConfig().getSupervisorsCount());

        Button next = new Button("Next");
        next.addStyleName("default");
        next.addClickListener(new Button.ClickListener() {

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
        back.addStyleName("default");
        back.addClickListener(new Button.ClickListener() {
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

        content.addComponent(clusterNameTxt);
        content.addComponent(zkClustersCombo);
        content.addComponent(masterNodeCombo);
        content.addComponent(nodesCountCmb);
        content.addComponent(buttons);

        setContent(layout);

    }

    private ComboBox makeMasterNodeComboBox(final Wizard wizard) {
        ComboBox cb = new ComboBox("Nimbus node");

        cb.setImmediate(true);
        cb.setTextInputAllowed(false);
        cb.setRequired(true);
        cb.setNullSelectionAllowed(false);
        cb.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Agent serverNode = (Agent)event.getProperty().getValue();
                wizard.getConfig().setNimbus(serverNode);
            }
        });
        return cb;
    }

    private void show(String notification) {
        Notification.show(notification);
    }

}
