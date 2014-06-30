/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.spark;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.ui.spark.manager.Manager;
import org.safehaus.subutai.ui.spark.wizard.Wizard;

/**
 * @author dilshat
 */
public class SparkForm extends CustomComponent {

	public SparkForm() {
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet mongoSheet = new TabSheet();
		mongoSheet.setSizeFull();
		Manager manager = new Manager();
		Wizard wizard = new Wizard();
		mongoSheet.addTab(wizard.getContent(), "Install");
		mongoSheet.addTab(manager.getContent(), "Manage");
		verticalLayout.addComponent(mongoSheet);

		setCompositionRoot(verticalLayout);
		manager.refreshClustersInfo();
	}

}
