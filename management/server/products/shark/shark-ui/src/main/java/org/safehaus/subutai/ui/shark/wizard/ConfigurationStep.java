/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.shark.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.ui.shark.SharkUI;

import java.util.List;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        ComboBox sparkClusters = new ComboBox("Spark cluster");

        sparkClusters.setMultiSelect(false);
        sparkClusters.setImmediate(true);
        sparkClusters.setTextInputAllowed(false);
        sparkClusters.setRequired(true);
        sparkClusters.setNullSelectionAllowed(false);

        List<Config> clusters = SharkUI.getSparkManager().getClusters();
        if (clusters.size() > 0) {
            for (Config info : clusters) {
                sparkClusters.addItem(info);
                sparkClusters.setItemCaption(info,
                        info.getClusterName());
            }
        }


        sparkClusters.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Config config = (Config) event.getProperty().getValue();
                    wizard.getConfig().setClusterName(config.getClusterName());
                    wizard.getConfig().setNodes(config.getAllNodes());
                }
            }
        });

        Config info = SharkUI.getSparkManager().getCluster(wizard.getConfig().getClusterName());

        if (info != null) {
            sparkClusters.setValue(info);
        } else if (clusters.size() > 0) {
            sparkClusters.setValue(clusters.iterator().next());
        }


        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Spark cluster");
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

        content.addComponent(sparkClusters);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
