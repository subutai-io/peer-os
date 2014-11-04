package org.safehaus.subutai.core.environment.ui.manage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings( "serial" )
public class EnvironmentsForm
{

    private static final String DESTROY = "Destroy";
    private static final String VIEW = "View";
    private static final String MANAGE = "Manage";
    private static final String CONFIGURE = "Configure";
    private static final String NAME = "Name";
    private static final String ENVIRONMENTS = "Environments";
    private static final String PROPERTIES = "Properties";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final String MANAGE_TITLE = "Manage environment containers";
    private static final String ID = "ID";
    private static final String STATUS = "Status";
    private static final String DATE_CREATED = "Date";
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
        table.addContainerProperty( ID, String.class, null );
        table.addContainerProperty( DATE_CREATED, String.class, null );
        table.addContainerProperty( STATUS, String.class, null );
        table.addContainerProperty( MANAGE, Button.class, null );
        table.addContainerProperty( CONFIGURE, Button.class, null );
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
            Button manageButton = new Button( MANAGE );
            manageButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Window window = envWindow( environment );
                    contentRoot.getUI().addWindow( window );
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
                        managerUI.getEnvironmentManager().destroyEnvironment( environment.getId() );
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
                    Window window = configWindow( environment );
                    contentRoot.getUI().addWindow( window );
                    window.setVisible( true );
                    Notification.show( CONFIGURE );
                }
            } );


            String cdate = getCreationDate( environment.getCreationTimestamp() );
            environmentsTable.addItem( new Object[] {
                    environment.getName(), environment.getId().toString(), cdate, environment.getStatus().toString(),
                    manageButton, configureButton, destroyButton
            }, environment.getId() );
        }
        environmentsTable.refreshRowCache();
    }


    private String getCreationDate( long ts )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd,yyyy HH:mm" );
        Date cdate = new Date( ts );
        return sdf.format( cdate );
    }


    private Window configWindow( final Environment environment )
    {
        Window window = createWindow( MANAGE_TITLE );
        window.setContent( genConfigureContainersTable( environment, environment.getContainers() ) );
        return window;
    }


    private VerticalLayout genConfigureContainersTable( Environment environment, Set<ContainerHost> containers )
    {
        VerticalLayout vl = new VerticalLayout();

        Table containersTable = new Table();
        containersTable.addContainerProperty( NAME, String.class, null );
        containersTable.addContainerProperty( "Peer", String.class, null );
        containersTable.addContainerProperty( "IP", TextField.class, null );
        containersTable.setPageLength( 10 );
        containersTable.setSelectable( false );
        containersTable.setEnabled( true );
        containersTable.setImmediate( true );
        containersTable.setSizeFull();

        for ( ContainerHost container : containers )
        {
            TextField field = new TextField();
            field.setWidth( "200px" );
            field.setValue( "should be ip" );
            containersTable.addItem( new Object[] {
                    container.getTemplateName(), container.getPeerId().toString(), field
            }, null );
        }


        vl.addComponent( containersTable );
        vl.addComponent( new Button( "Apply" ) );
        return vl;
    }


    private Window envWindow( Environment environment )
    {
        Window window = createWindow( MANAGE_TITLE );
        window.setContent( genContainersTable( environment, environment.getContainers() ) );
        return window;
    }


    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setWidth( "800px" );
        window.setHeight( "500px" );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }


    private VerticalLayout genContainersTable( Environment environment, Set<ContainerHost> containers )
    {
        VerticalLayout vl = new VerticalLayout();

        Table containersTable = new Table();
        containersTable.addContainerProperty( NAME, String.class, null );
        containersTable.addContainerProperty( "Peer", String.class, null );
        containersTable.addContainerProperty( PROPERTIES, Button.class, null );
        containersTable.addContainerProperty( START, Button.class, null );
        containersTable.addContainerProperty( STOP, Button.class, null );
        containersTable.addContainerProperty( DESTROY, Button.class, null );
        containersTable.setPageLength( 10 );
        containersTable.setSelectable( false );
        containersTable.setEnabled( true );
        containersTable.setImmediate( true );
        containersTable.setSizeFull();

        for ( ContainerHost container : containers )
        {

            containersTable.addItem( new Object[] {
                    container.getTemplateName(), container.getPeerId().toString(), propertiesButton( container ),
                    startButton( environment, container ), stopButton( environment, container ),
                    destroyButton( environment, container )
            }, null );
        }


        vl.addComponent( containersTable );
        return vl;
    }


    private Object propertiesButton( final ContainerHost container )
    {
        Button button = new Button( PROPERTIES );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Window window = createWindow( PROPERTIES );
                window.setContent( getContainerDetails( container ) );
                window.setWidth( "600px" );
                window.setHeight( "300px" );
                contentRoot.getUI().addWindow( window );
                window.setVisible( true );
                //                Notification.show( PROPERTIES );
            }
        } );
        return button;
    }


    private Table getContainerDetails( ContainerHost container )
    {
        Table table = new Table();
        table.setSizeFull();
        table.addContainerProperty( "Property", String.class, null );
        table.addContainerProperty( "Value", String.class, null );
        table.addItem( new Object[] { "Peer", container.getPeerId().toString() }, null );
        table.addItem( new Object[] { "Environment ID", container.getEnvironmentId().toString() }, null );
        return table;
    }


    private Object startButton( final Environment environment, final ContainerHost container )
    {
        Button button = new Button( START );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                /*try
                {
                    DefaultCommandMessage commandMessage = container.start();
                    environment.invoke( commandMessage );
                }
                catch ( ContainerException e )
                {
                    Notification.show( e.getMessage() );
                }*/
            }
        } );
        return button;
    }


    private Object stopButton( final Environment environment, final ContainerHost container )
    {
        Button button = new Button( STOP );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                /*try
                {

                    DefaultCommandMessage commandMessage = container.stop();
                    environment.invoke( commandMessage );
                }
                catch ( ContainerException e )
                {
                    Notification.show( e.getMessage() );
                }*/
            }
        } );
        return button;
    }


    private Object destroyButton( Environment environment, final ContainerHost container )
    {
        Button button = new Button( DESTROY );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                //TODO: destroy functionality
                //                DefaultCommandMessage commandMessage = container.start();
                //                environment.invoke( commandMessage );
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
