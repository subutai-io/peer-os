package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentDetails;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class EnvironmentsForm {

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentsForm( EnvironmentManagerUI managerUI ) {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments", 300 );

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
        table.addContainerProperty( "Owner", String.class, null );
        table.addContainerProperty( "Date", String.class, null );
        table.addContainerProperty( "Status", String.class, null );
        table.addContainerProperty( "Info", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        //        table.setWidth( 100, Sizeable.UNITS_PERCENTAGE );
        //        table.setHeight( size, Sizeable.UNITS_PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        //        table.addListener( new ItemClickEvent.ItemClickListener() {
        //
        //            public void itemClick( ItemClickEvent event ) {
        //                if ( event.isDoubleClick() ) {
        //
        //                }
        //            }
        //        } );
        return table;
    }


    private void updateTableData() {
        environmentsTable.removeAllItems();
        List<Environment> environmentList = managerUI.getEnvironmentManager().getEnvironments();
        for ( final Environment environment : environmentList ) {
            Button viewEnvironmentInfoButton = new Button( "Info" );
            viewEnvironmentInfoButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    EnvironmentDetails detailsWindow = new EnvironmentDetails( "Environment details" );
                    detailsWindow.setContent( environment );
                    contentRoot.getUI().addWindow( detailsWindow );
                    detailsWindow.setVisible( true );
                }
            } );

            final Object rowId = environmentsTable.addItem( new Object[] {
                    environment.getName(), "$user", "$date", "Good", viewEnvironmentInfoButton, new Button( "Delete" )
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
