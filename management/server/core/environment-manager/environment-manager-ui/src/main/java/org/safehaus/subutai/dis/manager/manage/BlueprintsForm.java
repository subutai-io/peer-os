package org.safehaus.subutai.dis.manager.manage;


import java.util.List;

import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.dis.manager.EnvironmentManagerUI;
import org.safehaus.subutai.dis.manager.window.BlueprintDetails;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class BlueprintsForm {

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManager environmentManager;


    public BlueprintsForm( EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;


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


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }


    private void updateTableData() {
        environmentsTable.removeAllItems();
        List<EnvironmentBlueprint> blueprints = environmentManager.getBlueprints();
        for ( final EnvironmentBlueprint blueprint : blueprints ) {

            final Button viewButton = new Button( "View" );
            viewButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    BlueprintDetails details = new BlueprintDetails( "Blueprint details", environmentManager );
                    details.setContent( blueprint );
                    contentRoot.getUI().addWindow( details );
                    details.setVisible( true );
                }
            } );

            final Button buildEnvironmentButton = new Button( "Build Environment" );
            buildEnvironmentButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    EnvironmentManagerUI.getExecutor().execute( new Runnable() {
                        @Override
                        public void run() {

                            environmentManager.buildEnvironment( blueprint );
                        }
                    } );
                }
            } );

            final Button deleteBlueprintButton = new Button( "Delete" );
            deleteBlueprintButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    environmentManager.deleteBlueprint( blueprint.getName() );
                }
            } );

            final Object rowId = environmentsTable.addItem( new Object[] {
                    blueprint.getName(), viewButton, buildEnvironmentButton, deleteBlueprintButton
            }, null );
        }
        environmentsTable.refreshRowCache();
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
}
