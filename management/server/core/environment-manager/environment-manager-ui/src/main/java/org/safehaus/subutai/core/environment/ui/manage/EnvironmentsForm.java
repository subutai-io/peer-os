package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentDetails;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class EnvironmentsForm {

    private final static Logger logger = Logger.getLogger( EnvironmentsForm.class.getName() );

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentsForm( final EnvironmentManagerUI managerUI ) {
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

        Button addEnvironmentButton = new Button( "Add" );
        addEnvironmentButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                Environment environment = new Environment( "environment" );

                managerUI.getEnvironmentManager().saveEnvironment( environment );
            }
        } );


        contentRoot.addComponent( addEnvironmentButton );
        contentRoot.addComponent( getEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size ) {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
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
        for ( final Environment environment : environmentList )
        {
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

            Button destroyEnvironment = new Button( "Destroy" );
            destroyEnvironment.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    try
                    {
                        managerUI.getEnvironmentManager().destroyEnvironment( environment.getUuid().toString() );
                    }
                    catch ( EnvironmentDestroyException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                }
            } );
            final Object rowId = environmentsTable.addItem( new Object[] {
                    environment.getName(), viewEnvironmentInfoButton, destroyEnvironment
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
