/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;

import java.util.Arrays;

/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout {

	public ConfigurationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(1, 3);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		final TextField clusterNameTxtFld = new TextField("Enter cluster name");
		clusterNameTxtFld.setInputPrompt("Cluster name");
		clusterNameTxtFld.setRequired(true);
		clusterNameTxtFld.setMaxLength(20);
		clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());
		clusterNameTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
			}
		});

		final TextField domainNameTxtFld = new TextField("Enter domain name");
		domainNameTxtFld.setInputPrompt("Domain name");
		domainNameTxtFld.setRequired(true);
		domainNameTxtFld.setMaxLength(20);
		domainNameTxtFld.setValue(wizard.getConfig().getClusterName());
		domainNameTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setDomainName(event.getProperty().getValue().toString().trim());
			}
		});

		final TextField dataDirectoryTxtFld = new TextField("Data directory");
		dataDirectoryTxtFld.setInputPrompt("/var/lib/cassandra/data");
		dataDirectoryTxtFld.setRequired(true);
		dataDirectoryTxtFld.setMaxLength(20);
		dataDirectoryTxtFld.setValue(wizard.getConfig().getClusterName());
		dataDirectoryTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setDataDirectory(event.getProperty().getValue().toString().trim());
			}
		});

		final TextField commitLogDirectoryTxtFld = new TextField("Commit log directory");
		commitLogDirectoryTxtFld.setInputPrompt("/var/lib/cassandra/commitlog");
		commitLogDirectoryTxtFld.setRequired(true);
		commitLogDirectoryTxtFld.setMaxLength(20);
		commitLogDirectoryTxtFld.setValue(wizard.getConfig().getClusterName());
		commitLogDirectoryTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setCommitLogDirectory(event.getProperty().getValue().toString().trim());
			}
		});

		final TextField savedCachesDirectoryTxtFld = new TextField("Saved caches directory");
		savedCachesDirectoryTxtFld.setInputPrompt("/var/lib/cassandra/saved_caches");
		savedCachesDirectoryTxtFld.setRequired(true);
		savedCachesDirectoryTxtFld.setMaxLength(20);
		savedCachesDirectoryTxtFld.setValue(wizard.getConfig().getClusterName());
		savedCachesDirectoryTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setSavedCachesDirectory(event.getProperty().getValue().toString().trim());
			}
		});

		//configuration servers number
		ComboBox nodesCountCombo = new ComboBox("Choose number of nodes in cluster", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//        nodesCountCombo.setMultiSelect(false);
		nodesCountCombo.setImmediate(true);
		nodesCountCombo.setTextInputAllowed(false);
		nodesCountCombo.setNullSelectionAllowed(false);
		nodesCountCombo.setValue(wizard.getConfig());

		nodesCountCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setNumberOfNodes((Integer) event.getProperty().getValue());
			}
		});

		//configuration servers number
		ComboBox seedsCountCombo = new ComboBox("Choose number of seeds", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//        seedsCountCombo.setMultiSelect(false);
		seedsCountCombo.setImmediate(true);
		seedsCountCombo.setTextInputAllowed(false);
		seedsCountCombo.setNullSelectionAllowed(false);
		seedsCountCombo.setValue(wizard.getConfig());

		seedsCountCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setNumberOfSeeds((Integer) event.getProperty().getValue());
			}
		});

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
					show("Please provide cluster name");
				} else if (Strings.isNullOrEmpty(wizard.getConfig().getDomainName())) {
					show("Please provide domain name");
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
		content.addComponent(domainNameTxtFld);
		content.addComponent(dataDirectoryTxtFld);
		content.addComponent(commitLogDirectoryTxtFld);
		content.addComponent(savedCachesDirectoryTxtFld);
		content.addComponent(nodesCountCombo);
		content.addComponent(seedsCountCombo);
		content.addComponent(buttons);

		addComponent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

}
