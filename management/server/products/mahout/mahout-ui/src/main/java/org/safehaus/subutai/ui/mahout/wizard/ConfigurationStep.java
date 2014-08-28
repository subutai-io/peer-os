/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mahout.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.mahout.MahoutUI;

import java.util.*;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

	private final TwinColSelect select;

	public ConfigurationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(1, 2);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		ComboBox hadoopClusters = new ComboBox("Hadoop cluster");
		select = new TwinColSelect("Nodes", new ArrayList<Agent>());

		hadoopClusters.setImmediate(true);
		hadoopClusters.setTextInputAllowed(false);
		hadoopClusters.setRequired(true);
		hadoopClusters.setNullSelectionAllowed(false);

		List<Config> clusters = MahoutUI.getHadoopManager().getClusters();

		if (clusters.size() > 0) {
			for (Config hadoopClusterInfo : clusters) {
				hadoopClusters.addItem(hadoopClusterInfo);
				hadoopClusters.setItemCaption(hadoopClusterInfo,
						hadoopClusterInfo.getClusterName());
			}
		}

		Config info = MahoutUI.getHadoopManager().getCluster(wizard.getConfig().getClusterName());

		if (info != null) {
			hadoopClusters.setValue(info);
		} else if (clusters.size() > 0) {
			hadoopClusters.setValue(clusters.iterator().next());
		}

		if (hadoopClusters.getValue() != null) {
			Config hadoopInfo = (Config) hadoopClusters.getValue();
			wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
			select.setContainerDataSource(
					new BeanItemContainer<>(
							Agent.class, hadoopInfo.getAllNodes())
			);
		}

		hadoopClusters.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Config hadoopInfo = (Config) event.getProperty().getValue();
					select.setValue(null);
					select.setContainerDataSource(
							new BeanItemContainer<>(
									Agent.class, hadoopInfo.getAllNodes())
					);
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
		select.setWidth(100, Unit.PERCENTAGE);
		select.setRequired(true);
		if (!Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
			select.setValue(wizard.getConfig().getNodes());
		}
		select.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
					wizard.getConfig().setNodes(agentList);
				}
			}
		});

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
					show("Please, select Hadoop cluster");
				} else if (Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
					show("Please, select target nodes");
				} else {
					wizard.next();
				}
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

		setContent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

}
