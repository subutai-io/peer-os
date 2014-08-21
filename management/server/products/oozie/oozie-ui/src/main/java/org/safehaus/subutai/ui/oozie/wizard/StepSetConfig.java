/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.ArrayList;
import java.util.Set;


/**
 * @author dilshat
 */
public class StepSetConfig extends Panel {

	public StepSetConfig(final Wizard wizard) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setMargin(true);

		GridLayout grid = new GridLayout(10, 10);
		grid.setSpacing(true);
		grid.setSizeFull();

		Panel panel = new Panel();
		Label menu = new Label("Oozie Installation Wizard");

		menu.setContentMode(ContentMode.HTML);
		panel.setContent(menu);
		grid.addComponent(menu, 0, 0, 2, 1);
		//		grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.setSpacing(true);

		Label configServersLabel = new Label("<strong>Oozie Server</strong>");
		configServersLabel.setContentMode(ContentMode.HTML);
		vl.addComponent(configServersLabel);

		final Label server = new Label("Server");
		vl.addComponent(server);

		final ComboBox cbServers = new ComboBox();
		cbServers.setImmediate(true);
		cbServers.setTextInputAllowed(false);
		cbServers.setRequired(true);
		cbServers.setNullSelectionAllowed(false);
		for (String agent : wizard.getConfig().getHadoopNodes()) {
			cbServers.addItem(agent);
			cbServers.setItemCaption(agent, agent);
		}

		vl.addComponent(cbServers);

		if (!Strings.isNullOrEmpty(wizard.getConfig().getServer())) {
			cbServers.setValue(wizard.getConfig().getServer());
		}

		final TwinColSelect selectClients = new TwinColSelect("", new ArrayList<String>());
		//		selectClients.setItemCaptionPropertyId("hostname");
		selectClients.setRows(7);
		selectClients.setNullSelectionAllowed(true);
		selectClients.setMultiSelect(true);
		selectClients.setImmediate(true);
		selectClients.setLeftColumnCaption("Available nodes");
		selectClients.setRightColumnCaption("Client nodes");
		selectClients.setWidth(100, Unit.PERCENTAGE);
		selectClients.setRequired(true);
		selectClients
				.setContainerDataSource(new BeanItemContainer<>(String.class, wizard.getConfig().getHadoopNodes()));

		if (!Util.isCollectionEmpty(wizard.getConfig().getClients())) {
			selectClients.setValue(wizard.getConfig().getClients());
		}

		vl.addComponent(selectClients);

		grid.addComponent(vl, 3, 0, 9, 9);
		grid.setComponentAlignment(vl, Alignment.TOP_CENTER);

		Button next = new Button("Next");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.getConfig().setServer((String) cbServers.getValue());
				wizard.getConfig().setClients((Set<String>) selectClients.getValue());

				if (Util.isCollectionEmpty(wizard.getConfig().getClients())) {
					show("Please select nodes for Oozie clients");
				} else if (wizard.getConfig().getServer() == null) {
					show("Please select node for Oozie server");
				} else {
					if (wizard.getConfig().getClients().contains(wizard.getConfig().getServer())) {
						show("Oozie server and client can not be installed on the same host");
					} else {
						wizard.next();
					}
				}
			}
		});

		Button back = new Button("Back");
		back.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.back();
			}
		});

		verticalLayout.addComponent(grid);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(back);
		horizontalLayout.addComponent(next);
		verticalLayout.addComponent(horizontalLayout);

		setContent(verticalLayout);
	}


	private void show(String notification) {
		Notification.show(notification);
	}
}
