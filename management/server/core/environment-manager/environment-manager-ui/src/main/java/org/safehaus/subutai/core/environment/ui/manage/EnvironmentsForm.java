package org.safehaus.subutai.core.environment.ui.manage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Peer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
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
    private static final String ADD_CONTAINERS = "Add containers";
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule managerUI;
    private Button environmentsButton;
    private ExecutorService executorService;


    public EnvironmentsForm( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;
        this.executorService = Executors.newCachedThreadPool();
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
        table.addContainerProperty( ADD_CONTAINERS, Button.class, null );
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
                    executorService
                            .execute( new DestroyEnvironmentTask( managerUI, environment.getId(), new CompleteEvent()
                            {
                                @Override
                                public void onComplete( final String status )
                                {
                                    Notification.show( status );
                                    environmentsButton.click();
                                }
                            } ) );
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
                }
            } );

            Button addContainersButton = new Button( ADD_CONTAINERS );
            addContainersButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Window window = addContainersWindow( environment );
                    contentRoot.getUI().addWindow( window );
                    window.setVisible( true );
                }
            } );


            String cdate = getCreationDate( environment.getCreationTimestamp() );
            environmentsTable.addItem( new Object[] {
                    environment.getName(), environment.getId().toString(), cdate, environment.getStatus().toString(),
                    manageButton, addContainersButton, configureButton, destroyButton
            }, environment.getId() );
        }
        environmentsTable.refreshRowCache();
    }


    private Window addContainersWindow( final Environment environment )
    {
        Window window = createWindow( "Add containers" );
        window.setContent( genAddContainersTable( environment ) );
        return window;
    }


    private VerticalLayout genAddContainersTable( final Environment environment )
    {
        VerticalLayout vl = new VerticalLayout();
        final ComboBox peersCombo = new ComboBox();
        List<Peer> peers = managerUI.getPeerManager().getPeers();
        //        BeanItemContainer<Peer> container = new BeanItemContainer<Peer>( Peer.class );
        peersCombo.addItems( peers );
        peersCombo.setCaption( "name" );
        vl.addComponent( peersCombo );
        final TextArea nodeGroupText = new TextArea();
        nodeGroupText.setRows( 12 );
        nodeGroupText.setSizeFull();
        nodeGroupText.setValue( getSampleNodeGroup() );
        vl.addComponent( nodeGroupText );
        Button createButton = new Button( "Add containers" );
        createButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                String ngJson = nodeGroupText.getValue();
                if ( ngJson.length() > 0 )
                {
                    Peer peer = ( Peer ) peersCombo.getValue();
                    try
                    {
                        managerUI.getEnvironmentManager()
                                 .createAdditionalContainers( environment.getId(), ngJson, peer );
                        Notification.show( "Containers created successfully" );
                    }
                    catch ( EnvironmentBuildException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                }
                else
                {
                    Notification.show( "Put node group json data" );
                }
            }
        } );
        vl.addComponent( createButton );
        return vl;
    }


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    private String getSampleNodeGroup()
    {
        NodeGroup ng = new NodeGroup();
        ng.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        ng.setTemplateName( "cassandra" );
        ng.setNumberOfNodes( 2 );
        ng.setExchangeSshKeys( true );
        ng.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        ng.setName( "New node group" );
        return GSON.toJson( ng );
    }


    private String getCreationDate( long ts )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd,yyyy HH:mm" );
        Date cdate = new Date( ts );
        return sdf.format( cdate );
    }


    private Window configWindow( final Environment environment )
    {
        Window window = createWindow( "Configure container network properties" );
        window.setContent( genConfigureContainersTable( environment ) );
        return window;
    }


    private VerticalLayout genConfigureContainersTable( Environment environment )
    {
        VerticalLayout vl = new VerticalLayout();
        CheckBox autoHostname = new CheckBox( "Assign auto Hostname" );
        vl.addComponent( autoHostname );
        CheckBox autoIP = new CheckBox( "Assign auto IP" );
        vl.addComponent( autoIP );

        Table containersTable = new Table();
        containersTable.addContainerProperty( NAME, String.class, null );
        containersTable.addContainerProperty( "Peer", String.class, null );
        containersTable.addContainerProperty( "Hostname", TextField.class, null );
        //        containersTable.addContainerProperty( "IP 1", TextField.class, null );
        containersTable.addContainerProperty( "IP 2", TextField.class, null );
        containersTable.setPageLength( 10 );
        containersTable.setSelectable( false );
        containersTable.setEnabled( true );
        containersTable.setImmediate( true );
        containersTable.setSizeFull();


        int ipInt = 10;
        for ( ContainerHost container : environment.getContainers() )
        {

            TextField fieldHostname = new TextField();
            fieldHostname.setWidth( "120px" );
            fieldHostname.setValue( container.getHostname() );

            /*TextField fieldIp = new TextField();
            fieldIp.setWidth( "120px" );
            fieldIp.setValue( container.getAgent().getListIP().get( 0 ) );*/

            TextField fieldIp2 = new TextField();
            fieldIp2.setWidth( "120px" );
            fieldIp2.setValue( "192.168.50." + ipInt++ );

            containersTable.addItem( new Object[] {
                    container.getTemplateName(), container.getPeerId().toString(), fieldHostname, /*fieldIp,*/ fieldIp2,
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
        window.setWidth( "900px" );
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
                    container.getHostname(), container.getPeerId().toString(), propertiesButton( container ),
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
