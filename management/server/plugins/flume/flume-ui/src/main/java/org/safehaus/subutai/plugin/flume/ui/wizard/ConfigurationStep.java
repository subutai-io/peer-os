package org.safehaus.subutai.plugin.flume.ui.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.ui.FlumeUI;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

public class ConfigurationStep extends VerticalLayout {

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label("Please, specify installation settings"));
        layout.addComponent(content);

        TextField txtClusterName = new TextField("Installation name: ");
        txtClusterName.setRequired(true);
        txtClusterName.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String v = event.getProperty().getValue().toString().trim();
                wizard.getConfig().setClusterName(v);
            }
        });
        txtClusterName.setValue(wizard.getConfig().getClusterName());

        content.addComponent(txtClusterName);

        SetupType st = wizard.getConfig().getSetupType();
        if(st == SetupType.OVER_HADOOP)
            addOverHadoopControls(content, wizard.getConfig());
        else if(st == SetupType.WITH_HADOOP)
            addWithHadoopControls(content);

        // --- buttons ---
        Button next = new Button("Next");
        next.addStyleName("default");
        next.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                nextButtonClickHandler(wizard);
            }
        });

        Button back = new Button("Back");
        back.addStyleName("default");
        back.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);

        content.addComponent(buttons);

        addComponent(layout);

    }

    private void addOverHadoopControls(ComponentContainer parent, final FlumeConfig config) {
        final TwinColSelect select = new TwinColSelect("Nodes", new ArrayList<Agent>());

        ComboBox hadoopClusters = new ComboBox("Hadoop cluster");
        hadoopClusters.setImmediate(true);
        hadoopClusters.setTextInputAllowed(false);
        hadoopClusters.setRequired(true);
        hadoopClusters.setNullSelectionAllowed(false);
        hadoopClusters.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    HadoopClusterConfig hadoopInfo = (HadoopClusterConfig)event.getProperty().getValue();
                    select.setValue(null);
                    select.setContainerDataSource(
                            new BeanItemContainer<>(Agent.class, hadoopInfo.getAllNodes())
                    );
                    config.setHadoopClusterName(hadoopInfo.getClusterName());
                    config.getNodes().clear();
                }
            }
        });

        Hadoop hadoopManager = FlumeUI.getHadoopManager();
        List<HadoopClusterConfig> clusters = hadoopManager.getClusters();
        if(clusters != null)
            for(HadoopClusterConfig hadoopClusterInfo : clusters) {
                hadoopClusters.addItem(hadoopClusterInfo);
                hadoopClusters.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }

        String hcn = config.getHadoopClusterName();
        if(hcn != null) {
            HadoopClusterConfig info = hadoopManager.getCluster(hcn);
            if(info != null) hadoopClusters.setValue(info);
        } else if(clusters.size() > 0)
            hadoopClusters.setValue(clusters.iterator().next());

        select.setItemCaptionPropertyId("hostname");
        select.setRows(7);
        select.setMultiSelect(true);
        select.setImmediate(true);
        select.setLeftColumnCaption("Available Nodes");
        select.setRightColumnCaption("Selected Nodes");
        select.setWidth(100, Unit.PERCENTAGE);
        select.setRequired(true);
        if(config.getNodes() != null && !config.getNodes().isEmpty())
            select.setValue(config.getNodes());
        select.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty().getValue() != null) {
                    Collection agentList = (Collection)event.getProperty().getValue();
                    config.getNodes().clear();
                    config.getNodes().addAll(agentList);
                }
            }
        });

        parent.addComponent(hadoopClusters);
        parent.addComponent(select);
    }

    private void addWithHadoopControls(ComponentContainer content) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void nextButtonClickHandler(Wizard wizard) {
        FlumeConfig config = wizard.getConfig();
        if(config.getClusterName() == null || config.getClusterName().isEmpty()) {
            show("Enter installation name");
            return;
        }
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            if(Util.isStringEmpty(wizard.getConfig().getHadoopClusterName()))
                show("Please, select Hadoop cluster");
            else if(Util.isCollectionEmpty(wizard.getConfig().getNodes()))
                show("Please, select target nodes");
            else
                wizard.next();
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            // TODO:
        } else show("Installation type not supported");
    }

    private void show(String notification) {
        Notification.show(notification);
    }

}
