package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings( "serial" )
public class EnvironmentsForm
{

    private static final String DESTROY = "Destroy";
    private static final String VIEW = "View";
    private static final String MANAGE = "Manage";
    private static final String CONFIGURE = "Configure";
    private static final String INFO = "Info";
    private static final String NAME = "Name";
    private static final String ENVIRONMENTS = "Environments";
    private static final String PROPERTIES = "Properties";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final String ENV_DETAILS = "Environment details";
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule managerUI;
    private Button environmentsButton;


    public EnvironmentsForm( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( ENVIRONMENTS, 300 );

        environmentsButton = new Button( VIEW );

        environmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );


        contentRoot.addComponent( environmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( NAME, String.class, null );
        table.addContainerProperty( INFO, Button.class, null );
        table.addContainerProperty( CONFIGURE, Button.class, null );
        table.addContainerProperty( MANAGE, Button.class, null );
        table.addContainerProperty( DESTROY, Button.class, null );
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
            Button viewButton = new Button( INFO );
            viewButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Window window = envWindow( environment );
                    window.setVisible( true );
                }
            } );

            Button destroyButton = new Button( DESTROY );
            destroyButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    try
                    {
                        managerUI.getEnvironmentManager().destroyEnvironment( environment.getUuid().toString() );
                        environmentsButton.click();
                    }
                    catch ( EnvironmentDestroyException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                }
            } );
            Button configureButton = new Button( CONFIGURE );
            configureButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Notification.show( CONFIGURE );
                }
            } );

            Button manageButton = new Button( MANAGE );
            manageButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Notification.show( "Backup/Move/Manage" );
                }
            } );

            environmentsTable.addItem( new Object[] {
                    environment.getName(), viewButton, manageButton, configureButton, destroyButton
            }, environment.getUuid() );
        }
        environmentsTable.refreshRowCache();
    }


    private Window envWindow( Environment environment )
    {
        Window window = createWindow( ENV_DETAILS );
        window.setContent( genContainersTable( environment.getContainers() ) );
        contentRoot.getUI().addWindow( window );
        return window;
    }


    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setWidth( "800px" );
        window.setHeight( "600px" );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }


    private VerticalLayout genContainersTable( Set<EnvironmentContainer> containers )
    {
        VerticalLayout vl = new VerticalLayout();

        Table containersTable = new Table();
        containersTable.addContainerProperty( NAME, String.class, null );
        containersTable.addContainerProperty( PROPERTIES, Button.class, null );
        containersTable.addContainerProperty( START, Button.class, null );
        containersTable.addContainerProperty( STOP, Button.class, null );
        containersTable.addContainerProperty( DESTROY, Button.class, null );
        containersTable.setPageLength( 10 );
        containersTable.setSelectable( false );
        containersTable.setEnabled( true );
        containersTable.setImmediate( true );
        containersTable.setSizeFull();

        for ( Container container : containers )
        {

            containersTable.addItem( new Object[] {
                    container.getName(), propertiesButton( container ), startButton( container ),
                    stopButton( container ), destroyButton( container )
            }, null );
        }


        vl.addComponent( containersTable );
        return vl;
    }


    private Object propertiesButton( final Container container )
    {
        Button button = new Button( PROPERTIES );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Notification.show( PROPERTIES );
            }
        } );
        return button;
    }


    private Object startButton( final Container container )
    {
        Button button = new Button( START );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                try
                {

                    if ( container.start() )
                    {
                        Notification.show( "Started" );
                    }
                    else
                    {
                        Notification.show( "Failed to start a container" );
                    }
                }
                catch ( ContainerException e )
                {
                    Notification.show( e.getMessage() );
                }
            }
        } );
        return button;
    }


    private Object stopButton( final Container container )
    {
        Button button = new Button( STOP );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                try
                {

                    if ( container.stop() )
                    {
                        Notification.show( "Stopped" );
                    }
                    else
                    {
                        Notification.show( "Failed to stop a container" );
                    }
                }
                catch ( ContainerException e )
                {
                    Notification.show( e.getMessage() );
                }
            }
        } );
        return button;
    }


    private Object destroyButton( final Container container )
    {
        Button button = new Button( DESTROY );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Notification.show( DESTROY );
            }
        } );
        return button;
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
