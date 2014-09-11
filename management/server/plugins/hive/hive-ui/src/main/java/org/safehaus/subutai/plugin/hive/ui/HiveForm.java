package org.safehaus.subutai.plugin.hive.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.hive.ui.manager.Manager;
import org.safehaus.subutai.plugin.hive.ui.wizard.Wizard;

public class HiveForm extends CustomComponent {

	private final Wizard wizard;
	private final Manager manager;

	public HiveForm() {
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
