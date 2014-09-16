package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.BlueprintDetails;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class BlueprintsForm {

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerUI managerUI;


    public BlueprintsForm( EnvironmentManagerUI managerUI ) {
        this.managerUI = managerUI;


        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Blueprints", 300 );

        Button getEnvironmentsButton = new Button( "View" );

        getEnvironmentsButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                updateTableData();
            }
        } );

        contentRoot.addComponent( getEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size ) {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "View", Button.class, null );
        table.addContainerProperty( "Build environment", Button.class, null );
        table.addContainerProperty( "Delete", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void updateTableData() {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildTask> environmentBuildTasks = managerUI.getEnvironmentManager().getBlueprints();
        if ( environmentBuildTasks.size() > 0 )
        {
            for ( final EnvironmentBuildTask environmentBuildTask : environmentBuildTasks )
            {

                final Button viewButton = new Button( "View" );
                viewButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent ) {
                        BlueprintDetails details =
                                new BlueprintDetails( "Blueprint details", managerUI.getEnvironmentManager() );
                        details.setContent( environmentBuildTask.getEnvironmentBlueprint() );
                        contentRoot.getUI().addWindow( details );
                        details.setVisible( true );
                    }
                } );

                final Button buildEnvironmentButton = new Button( "Build Environment" );
                buildEnvironmentButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent ) {
                        WizardWindow wizardWindow = new WizardWindow( "Wizard", managerUI, environmentBuildTask );
                        contentRoot.getUI().addWindow( wizardWindow );
                        wizardWindow.setVisible( true );
                    }
                } );

                final Button deleteBlueprintButton = new Button( "Delete" );
                deleteBlueprintButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent ) {
                        managerUI.getEnvironmentManager().deleteBlueprint( environmentBuildTask.getUuid().toString() );
                    }
                } );

                final Object rowId = environmentsTable.addItem( new Object[] {
                        environmentBuildTask.getEnvironmentBlueprint().getName(), viewButton, buildEnvironmentButton,
                        deleteBlueprintButton
                }, null );
            }
        }
        else
        {
            Notification.show( "There is no blueprints", "No blueprints found", Notification.Type.WARNING_MESSAGE );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
