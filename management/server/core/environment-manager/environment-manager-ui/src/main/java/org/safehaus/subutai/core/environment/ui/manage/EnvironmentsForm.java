package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class EnvironmentsForm
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentsForm.class.getName() );
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule managerUI;


    public EnvironmentsForm( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments", 300 );

        Button getEnvironmentsButton = new Button( "View" );

        getEnvironmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );


        contentRoot.addComponent( getEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Info", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void updateTableData()
    {
        environmentsTable.removeAllItems();
        List<Environment> environmentList = managerUI.getEnvironmentManager().getEnvironments();
        for ( final Environment environment : environmentList )
        {
            Button viewEnvironmentInfoButton = new Button( "Info" );
            viewEnvironmentInfoButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    EnvironmentDetails detailsWindow = new EnvironmentDetails( "Environment details" );
                    detailsWindow.setContent( genContainersTable() );
                    contentRoot.getUI().addWindow( detailsWindow );
                    detailsWindow.setVisible( true );
                }


                private VerticalLayout genContainersTable()
                {
                    VerticalLayout vl = new VerticalLayout();

                    Table containersTable = new Table();
                    containersTable.addContainerProperty( "Name", String.class, null );
                    containersTable.addContainerProperty( "Destroy", Button.class, null );
                    containersTable.setPageLength( 10 );
                    containersTable.setSelectable( false );
                    containersTable.setEnabled( true );
                    containersTable.setImmediate( true );
                    containersTable.setSizeFull();


                    Set<EnvironmentContainer> containers = environment.getContainers();
                    for ( Container container : containers )
                    {

                        containersTable.addItem( new Object[] {
                                container.getName(), new Button( "Destroy" )
                        }, null );
                    }


                    vl.addComponent( containersTable );
                    return vl;
                }
            } );

            Button destroyEnvironment = new Button( "Destroy" );
            destroyEnvironment.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
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
            environmentsTable.addItem( new Object[] {
                    environment.getName(), viewEnvironmentInfoButton, destroyEnvironment
            }, environment.getUuid() );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
