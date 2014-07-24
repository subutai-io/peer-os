/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hadoop.ui.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.ArrayList;
import java.util.List;


/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout {

	public ConfigurationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(2, 7);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		final TextField clusterNameTxtFld = new TextField("Enter cluster name");
		clusterNameTxtFld.setInputPrompt("Cluster name");
		clusterNameTxtFld.setRequired(true);
		clusterNameTxtFld.setMaxLength(20);
		if (!Strings.isNullOrEmpty(wizard.getHadoopClusterConfig().getClusterName())) {
			clusterNameTxtFld.setValue(wizard.getHadoopClusterConfig().getClusterName());
		}
		clusterNameTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getHadoopClusterConfig().setClusterName(event.getProperty().getValue().toString().trim());
			}
		});

		//configuration servers number
		List<Integer> s = new ArrayList<Integer>();
		for (int i = 0; i < 50; i++) {
			s.add(i);
		}

		ComboBox slaveNodesComboBox = new ComboBox("Choose number of slave nodes", s);
//        slaveNodesComboBox.setMultiSelect(false);
		slaveNodesComboBox.setImmediate(true);
		slaveNodesComboBox.setTextInputAllowed(false);
		slaveNodesComboBox.setNullSelectionAllowed(false);
		slaveNodesComboBox.setValue(wizard.getHadoopClusterConfig().getCountOfSlaveNodes());

		slaveNodesComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getHadoopClusterConfig().setCountOfSlaveNodes((Integer) event.getProperty().getValue());
			}
		});

		//configuration replication factor
		ComboBox replicationFactorComboBox = new ComboBox("Choose replication factor for slave nodes", s);
//        replicationFactorComboBox.setMultiSelect(false);
		replicationFactorComboBox.setImmediate(true);
		replicationFactorComboBox.setTextInputAllowed(false);
		replicationFactorComboBox.setNullSelectionAllowed(false);
		replicationFactorComboBox.setValue(wizard.getHadoopClusterConfig().getReplicationFactor());

		replicationFactorComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getHadoopClusterConfig().setReplicationFactor((Integer) event.getProperty().getValue());
			}
		});

		TextField domain = new TextField("Enter domain name");
		domain.setInputPrompt(wizard.getHadoopClusterConfig().getDomainName());
		domain.setValue(wizard.getHadoopClusterConfig().getDomainName());
		domain.setMaxLength(20);
		domain.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				String value = event.getProperty().getValue().toString().trim();
				if (!Util.isStringEmpty(value)) {
					wizard.getHadoopClusterConfig().setDomainName(value);
				}
			}
		});

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (Util.isStringEmpty(wizard.getHadoopClusterConfig().getClusterName())) {
					show("Please provide cluster name");
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

		content.addComponent(clusterNameTxtFld);
		content.addComponent(domain);
		content.addComponent(slaveNodesComboBox);
		content.addComponent(replicationFactorComboBox);
		content.addComponent(buttons);

		addComponent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

}
