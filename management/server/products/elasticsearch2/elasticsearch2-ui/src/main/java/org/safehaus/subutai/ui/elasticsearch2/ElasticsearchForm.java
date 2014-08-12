package org.safehaus.subutai.ui.elasticsearch2;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import org.safehaus.subutai.ui.elasticsearch2.manager.Manager;
import org.safehaus.subutai.ui.elasticsearch2.wizard.Wizard;

public class ElasticsearchForm extends CustomComponent {

	private final Wizard wizard;
	private final Manager manager;

	public ElasticsearchForm() {
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		setCompositionRoot(verticalLayout);

		TabSheet sheet = new TabSheet();
		sheet.setSizeFull();
		manager = new Manager();
		wizard = new Wizard();
		sheet.addTab(wizard.getContent(), "Install");
		sheet.addTab(manager.getContent(), "Manage");
		verticalLayout.addComponent(sheet);

		manager.refreshClustersInfo();
	}

}
