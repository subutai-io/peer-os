package org.safehaus.subutai.dis.manager.manage;


import java.util.List;

import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class EnvironmentsForm {

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManager environmentManager;


    public EnvironmentsForm( EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments", 300 );

        Button getEnvironmentsButton = new Button( "View" );

        getEnvironmentsButton.addListener( new Button.ClickListener() {
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
        List<Environment> environmentList = environmentManager.getEnvironments();
        for ( Environment environment : environmentList ) {
            final Object rowId = environmentsTable.addItem( new Object[] {
                    environment.getName(), "$user", "$date", "Good", new Button( "Info" ), new Button( "Delete" )
            }, null );
        }
        environmentsTable.refreshRowCache();
        environmentsTable.requestRepaint();
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
}
