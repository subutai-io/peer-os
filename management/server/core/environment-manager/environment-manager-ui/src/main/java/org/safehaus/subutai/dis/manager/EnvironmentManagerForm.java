/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.dis.manager;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.dis.manager.manage.BlueprintLoadForm;
import org.safehaus.subutai.dis.manager.manage.BlueprintsForm;
import org.safehaus.subutai.dis.manager.manage.EnvironmentsForm;
import org.safehaus.subutai.shared.protocol.Disposable;


/**
 *
 */
public class EnvironmentManagerForm extends CustomComponent implements Disposable {


	private BlueprintLoadForm blueprintManager;
	private BlueprintsForm blueprintsForm;
	private EnvironmentsForm environmentForm;


	public EnvironmentManagerForm(EnvironmentManager environmentManager) {
		setHeight(100, UNITS_PERCENTAGE);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		TabSheet sheet = new TabSheet();
		sheet.setStyleName(Runo.TABSHEET_SMALL);
		sheet.setSizeFull();
		blueprintManager = new BlueprintLoadForm(environmentManager);
		blueprintsForm = new BlueprintsForm(environmentManager);
		environmentForm = new EnvironmentsForm(environmentManager);
		sheet.addTab(blueprintManager.getContentRoot(), "Blueprint load");
		sheet.addTab(blueprintsForm.getContentRoot(), "Blueprints");
		sheet.addTab(environmentForm.getContentRoot(), "Environments");
		verticalLayout.addComponent(sheet);


		setCompositionRoot(verticalLayout);
	}


	public void dispose() {
	}
}
