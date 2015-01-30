package org.safehaus.subutai.core.environment.ui.manage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
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
    private static final String CONTAINER_QUOTA = "%s container quota description";
    private static final String ID = "ID";
    private static final String STATUS = "Status";
    private static final String DATE_CREATED = "Date";
    private static final String ADD_CONTAINERS = "Add containers";
    private static final String QUOTA = "Quota";
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger( EnvironmentsForm.class );
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
        environmentsButton.setId( "environmentsButton" );

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
        for ( ContainerHost container : environment.getContainerHosts() )
        {
            TextField fieldHostname = new TextField();
            fieldHostname.setWidth( "120px" );
            fieldHostname.setValue( container.getHostname() );

            TextField fieldIp2 = new TextField();
            fieldIp2.setWidth( "120px" );
            fieldIp2.setValue( "192.168.50." + ipInt++ );

            containersTable.addItem( new Object[] {
                    container.getTemplateName(), container.getPeerId(), fieldHostname, fieldIp2,
            }, null );
        }


        vl.addComponent( containersTable );
        vl.addComponent( new Button( "Apply" ) );
        return vl;
    }


    private Window envWindow( Environment environment )
    {
        Window window = createWindow( MANAGE_TITLE );
        window.setContent( genContainersTable( environment, environment.getContainerHosts() ) );
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
        containersTable.addContainerProperty( QUOTA, Button.class, null );
        containersTable.setPageLength( 10 );
        containersTable.setSelectable( false );
        containersTable.setEnabled( true );
        containersTable.setImmediate( true );
        containersTable.setSizeFull();

        for ( ContainerHost container : containers )
        {
            containersTable.addItem( new Object[] {
                    container.getHostname(), container.getPeerId(), propertiesButton( container ),
                    startButton( environment, container ), stopButton( environment, container ),
                    destroyButton( environment, container ), getQuotaButton( environment, container )
            }, null );
        }


        vl.addComponent( containersTable );
        vl.addComponent( genPublicKeyElements( environment ) );
        return vl;
    }


    private Component genPublicKeyElements( final Environment env )
    {
        VerticalLayout vl = new VerticalLayout();

        final TextArea txtPublicKey = new TextArea( "Public key of the environment" );
        txtPublicKey.setStyleName( "default" );
        txtPublicKey.setWidth( 70, Sizeable.Unit.PERCENTAGE );
        txtPublicKey.setRows( 10 );
        if ( env.getPublicKey() != null )
        {
            txtPublicKey.setValue( env.getPublicKey() );
        }

        Button btn = new Button( "Save public key" );
        btn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                env.setPublicKey( txtPublicKey.getValue() );
                managerUI.getEnvironmentManager().saveEnvironment( env );
            }
        } );

        vl.addComponent( txtPublicKey );
        vl.addComponent( btn );

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
        table.addItem( new Object[] {
                "Peer", container.getPeerId()
        }, null );
        table.addItem( new Object[] {
                "Environment ID", container.getEnvironmentId()
        }, null );
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
                /* try { DefaultCommandMessage commandMessage = container.start(); environment.invoke( commandMessage );
                 * } catch ( ContainerException e ) { Notification.show( e.getMessage() );
                } */
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
                /* try {
                 *
                 * DefaultCommandMessage commandMessage = container.stop(); environment.invoke( commandMessage ); }
                 * catch ( ContainerException e ) { Notification.show( e.getMessage() );
                } */
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


    private Button getQuotaButton( final Environment environment, final ContainerHost containerHost )
    {
        Button button = new Button( QUOTA );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                //                try
                //                {
                //                    PeerQuotaInfo peerQuotaInfo = containerHost.getQuota( QuotaType
                // .QUOTA_TYPE_ALL_JSON );
                Window window = getContainerQuotaWindow( containerHost, null );
                    contentRoot.getUI().addWindow( window );
                    window.setVisible( true );
                //                }
                ////                catch ( PeerException e )
                ////                {
                ////                    LOGGER.error( "Error retrieving quota for container.", e );
                //                }
            }
        } );
        return button;
    }


    private Window getContainerQuotaWindow( ContainerHost containerHost, PeerQuotaInfo containerQuotaInfo )
    {
        Window window = createWindow( String.format( CONTAINER_QUOTA, containerHost.getHostname() ) );
        window.setContent( getContainerQuotaLayout( containerQuotaInfo ) );
        return window;
    }


    private VerticalLayout getContainerQuotaLayout( final PeerQuotaInfo containerQuotaInfo )
    {
        VerticalLayout vl = new VerticalLayout();
        final Label value = new Label( "0" );
        //        value.setWidth( "3em" );
        value.setImmediate( true );

        //        CpuQuotaInfo cpuQuotaInfo;
        //        HddQuotaInfo homePartitionQuota;
        //        HddQuotaInfo varPartitionQuota;
        //        HddQuotaInfo optPartitionQuota;
        //        HddQuotaInfo rootfsPartitionQuota;
        //        MemoryQuotaInfo memoryQuota;

        final Slider cpuSlider = new Slider( "Select a value between 0 and 100" );
        cpuSlider.setWidth( "50%" );
        cpuSlider.setMin( 0 );
        cpuSlider.setMax( 100 );
        cpuSlider.setImmediate( true );
        cpuSlider.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        final Slider homePartition = new Slider( "Select a value between 0 and 100" );
        homePartition.setWidth( "50%" );
        homePartition.setMin( 0 );
        homePartition.setMax( 100 );
        homePartition.setImmediate( true );
        homePartition.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        final Slider varPartition = new Slider( "Select a value between 0 and 100" );
        varPartition.setWidth( "50%" );
        varPartition.setMin( 0 );
        varPartition.setMax( 100 );
        varPartition.setImmediate( true );
        varPartition.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        final Slider optPartition = new Slider( "Select a value between 0 and 100" );
        optPartition.setWidth( "50%" );
        optPartition.setMin( 0 );
        optPartition.setMax( 100 );
        optPartition.setImmediate( true );
        optPartition.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        final Slider rootfsPartition = new Slider( "Select a value between 0 and 100" );
        rootfsPartition.setWidth( "50%" );
        rootfsPartition.setMin( 0 );
        rootfsPartition.setMax( 100 );
        rootfsPartition.setImmediate( true );
        rootfsPartition.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        final Slider memorySlider = new Slider( "Select a value between 0 and 100" );
        memorySlider.setWidth( "50%" );
        memorySlider.setMin( 0 );
        memorySlider.setMax( 100 );
        memorySlider.setImmediate( true );
        memorySlider.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                value.setValue( event.getProperty().getValue().toString() );
                Integer percentage = Integer.parseInt( event.getProperty().getValue().toString() );
                //                containerQuotaInfo.getCpuQuotaInfo().setPercentage( percentage );
            }
        } );

        vl.addComponent( cpuSlider );
        vl.addComponent( homePartition );
        vl.addComponent( varPartition );
        vl.addComponent( optPartition );
        vl.addComponent( rootfsPartition );
        vl.addComponent( memorySlider );
        //        vl.setExpandRatio( cpuSlider, 1 );
        vl.addComponent( value );
        vl.setComponentAlignment( value, Alignment.BOTTOM_LEFT );

        return vl;
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}

