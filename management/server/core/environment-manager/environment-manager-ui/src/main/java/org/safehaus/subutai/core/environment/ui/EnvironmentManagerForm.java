/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.ui.manage.BlueprintLoadForm;
import org.safehaus.subutai.core.environment.ui.manage.BlueprintsForm;
import org.safehaus.subutai.core.environment.ui.manage.EnvironmentsForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class EnvironmentManagerForm extends CustomComponent implements Disposable {


    private BlueprintLoadForm blueprintManager;
    private BlueprintsForm blueprintsForm;
    private EnvironmentsForm environmentForm;


    public EnvironmentManagerForm( EnvironmentManagerUI managerUI ) {
        setHeight( 100, UNITS_PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        blueprintManager = new BlueprintLoadForm( managerUI );
        blueprintsForm = new BlueprintsForm( managerUI );
        environmentForm = new EnvironmentsForm( managerUI );
        sheet.addTab( blueprintManager.getContentRoot(), "Blueprint load" );
        sheet.addTab( blueprintsForm.getContentRoot(), "Blueprints" );
        sheet.addTab( environmentForm.getContentRoot(), "Environments" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    public void dispose() {
    }
}
