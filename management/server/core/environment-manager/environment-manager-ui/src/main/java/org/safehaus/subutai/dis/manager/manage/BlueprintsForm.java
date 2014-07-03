package org.safehaus.subutai.dis.manager.manage;


import java.util.List;

import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.dis.manager.window.BlueprintDetails;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
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
        List<Blueprint> blueprints = environmentManager.getBlueprints();
        for ( Blueprint blueprint : blueprints ) {
            final ViewButton viewButton = new ViewButton( "View", blueprint );
            viewButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    BlueprintDetails details = new BlueprintDetails();

                    details.setBlueprint( viewButton.getBlueprint() );
                    details.setVisible( true );
                }
            } );

            final Object rowId = environmentsTable.addItem( new Object[] {

                    blueprint.getName(), viewButton, new Button( "Build environment" ), new Button( "Delete" )
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


    private class ViewButton extends Button {

        Blueprint blueprint;


        private ViewButton( final String caption, final Blueprint blueprint ) {
            super( caption );
            this.blueprint = blueprint;
        }


        public Blueprint getBlueprint() {
            return blueprint;
        }
    }
}
