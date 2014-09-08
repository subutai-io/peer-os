package org.safehaus.subutai.plugin.pig.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.pig.ui.manager.Manager;
import org.safehaus.subutai.plugin.pig.ui.wizard.Wizard;

public class PigForm extends CustomComponent {

	public PigForm() {
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
