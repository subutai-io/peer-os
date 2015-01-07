/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.environment.ui.manage.BlueprintUploadForm;
import org.safehaus.subutai.core.environment.ui.manage.BlueprintsForm;
import org.safehaus.subutai.core.environment.ui.manage.EnvironmentsBuildProcessForm;
import org.safehaus.subutai.core.environment.ui.manage.EnvironmentsForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class EnvironmentManagerComponent extends CustomComponent implements Disposable
{
    private BlueprintUploadForm blueprintManager;
    private BlueprintsForm blueprintsForm;
    private EnvironmentsForm environmentForm;
    private EnvironmentsBuildProcessForm environmentBuildForm;


    public EnvironmentManagerComponent( EnvironmentManagerPortalModule managerUI )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        blueprintManager = new BlueprintUploadForm( managerUI );
        blueprintsForm = new BlueprintsForm( managerUI );
        environmentBuildForm = new EnvironmentsBuildProcessForm( managerUI );
        environmentForm = new EnvironmentsForm( managerUI );
        sheet.addTab( blueprintManager.getContentRoot(), "Blueprint load" );
        sheet.getTab( 0 ).setId( "BlueprintLoad" );
        sheet.addTab( blueprintsForm.getContentRoot(), "Blueprints" );
        sheet.getTab( 1 ).setId( "Blueprints" );
        sheet.addTab( environmentBuildForm.getContentRoot(), "Build process" );
        sheet.getTab( 2 ).setId( "BuildProcess" );
        sheet.addTab( environmentForm.getContentRoot(), "Environments" );
        sheet.getTab( 3 ).setId( "Environments" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    public void dispose()
    {
        this.blueprintManager = null;
        this.blueprintsForm = null;
        this.environmentForm = null;
        this.environmentBuildForm = null;
    }
}
