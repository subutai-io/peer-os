/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.plugin.oozie.ui.manager.Manager;
import org.safehaus.subutai.plugin.oozie.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 * @author dilshat
 */
public class OozieForm extends CustomComponent {

    private AgentManager agentManager;
	private final Wizard wizard;
	private final Manager manager;

	public OozieForm() {
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet mongoSheet = new TabSheet();
		mongoSheet.setSizeFull();
		manager = new Manager();
		wizard = new Wizard();
		mongoSheet.addTab(wizard.getContent(), "Install");
		mongoSheet.addTab(manager.getContent(), "Manage");
		verticalLayout.addComponent(mongoSheet);

		setCompositionRoot(verticalLayout);
		manager.refreshClustersInfo();
	}

}
