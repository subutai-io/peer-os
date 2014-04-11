package org.safehaus.kiskis.mgmt.ui.flume.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.flume.FlumeUI;

public class ConfigurationStep extends Panel {

    private final ComboBox hadoopClusters;
    private final TwinColSelect select;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        hadoopClusters = new ComboBox("Hadoop cluster");
        select = new TwinColSelect("Nodes", new ArrayList<Agent>());

        hadoopClusters.setMultiSelect(false);
        hadoopClusters.setImmediate(true);
        hadoopClusters.setTextInputAllowed(false);
        hadoopClusters.setRequired(true);
        hadoopClusters.setNullSelectionAllowed(false);

        List<HadoopClusterInfo> clusters = FlumeUI.getDbManager().
                getInfo(HadoopClusterInfo.SOURCE, HadoopClusterInfo.class);
        if(clusters.size() > 0) {
            for(HadoopClusterInfo hadoopClusterInfo : clusters) {
                hadoopClusters.addItem(hadoopClusterInfo);
                hadoopClusters.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        HadoopClusterInfo info = FlumeUI.getDbManager().
                getInfo(HadoopClusterInfo.SOURCE,
                        wizard.getConfig().getClusterName(),
                        HadoopClusterInfo.class);

        if(info != null) {
            hadoopClusters.setValue(info);
        } else if(clusters.size() > 0) {
            hadoopClusters.setValue(clusters.iterator().next());
        }

        if(hadoopClusters.getValue() != null) {
            HadoopClusterInfo hadoopInfo = (HadoopClusterInfo)hadoopClusters.getValue();
            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
            select.setContainerDataSource(
                    new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllAgents()));
        }

        hadoopClusters.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    HadoopClusterInfo hadoopInfo = (HadoopClusterInfo)event.getProperty().getValue();
                    select.setValue(null);
                    select.setContainerDataSource(
                            new BeanItemContainer<Agent>(
                                    Agent.class, hadoopInfo.getAllAgents()));
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                    wizard.getConfig().setNodes(new HashSet<Agent>());
                }
            }
        });

        select.setItemCaptionPropertyId("hostname");
        select.setRows(7);
        select.setMultiSelect(true);
        select.setImmediate(true);
        select.setLeftColumnCaption("Available Nodes");
        select.setRightColumnCaption("Selected Nodes");
        select.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        select.setRequired(true);
        if(!Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
            select.setValue(wizard.getConfig().getNodes());
        }
        select.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection)event.getProperty().getValue());
                    wizard.getConfig().setNodes(agentList);
                }
            }
        });

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if(Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Hadoop cluster");
                } else if(Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
                    show("Please, select target nodes");
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
        content.addComponent(select);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
