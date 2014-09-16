package org.safehaus.subutai.plugin.hadoop.ui;

import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.hadoop.ui.manager.Manager;
import org.safehaus.subutai.plugin.hadoop.ui.wizard.Wizard;

/**
 * Created by daralbaev on 08.04.14.
 */
public class HadoopForm extends CustomComponent {
	private final Wizard wizard;
	private final Manager manager;

	public HadoopForm(ExecutorService executorService, ServiceLocator serviceLocator) throws NamingException{
		setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet sheet = new TabSheet();
		sheet.setSizeFull();

		manager = new Manager(executorService, serviceLocator);
		wizard = new Wizard(executorService, serviceLocator);
		sheet.addTab(wizard.getContent(), "Install");
		sheet.addTab(manager, "Manage");


		verticalLayout.addComponent(sheet);
		setCompositionRoot(verticalLayout);
	}
}
