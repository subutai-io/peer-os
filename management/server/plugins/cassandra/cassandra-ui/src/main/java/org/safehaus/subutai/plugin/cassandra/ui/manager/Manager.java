/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.hostregistry.api.Interface;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.api.NodeOperationTask;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String START_ALL_BUTTON_CAPTION = "Start All";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Environment";
    protected static final String REMOVE_CLUSTER = "Remove Cluster";
    protected static final String DESTROY_NODE_BUTTON_CAPTION = "Destroy";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String BUTTON_STYLE_NAME = "default";
    private static final String MESSAGE = "No cluster is installed !";
    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn, addNodeBtn, removeCluster;
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;
    private final Cassandra cassandra;
    private final Table nodesTable;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private CassandraClusterConfig config;


    public Manager( final ExecutorService executorService, Cassandra cassandra, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {

        this.cassandra = cassandra;
        this.executorService = executorService;
        this.tracker = tracker;
        this.environmentManager = environmentManager;

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Cluster nodes" );
        nodesTable.setId( "CassNodeTable" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );
        controlsContent.setHeight( 100, Sizeable.Unit.PERCENTAGE );


        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
        controlsContent.setComponentAlignment( clusterNameLabel, Alignment.MIDDLE_CENTER );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "CassClusterComboBox" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( CassandraClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );
        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );

        /** Refresh button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "CassRefreshClustersBtn" );
        addClickListener( refreshClustersBtn );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        /** Check all button */
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "CassCheckAllBtn" );
        addClickListener( checkAllBtn );
        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        /** Start all button */
        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.setId( "CassStartAllBtn" );
        addClickListener( startAllBtn );
        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );


        /** Stop all button */
        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.setId( "CassStopAllBtn" );
        addClickListener( stopAllBtn );
        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );


        /** Destroy Cluster button */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "CassDestroyClusterBtn" );
        destroyClusterBtn.setDescription( "Destroy environment with containers" );
        addClickListenerToDestroyClusterButton();
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );


        /** Remove Cluster button */
        removeCluster = new Button( REMOVE_CLUSTER );
        removeCluster.setId( "CassRemoveClusterBtn" );
        removeCluster.setDescription( "Removes cluster info from DB" );
        addClickListenerToRemoveClusterButton();
        controlsContent.addComponent( removeCluster );
        controlsContent.setComponentAlignment( removeCluster, Alignment.MIDDLE_CENTER );


        /** Add Node button */
        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId( "CassAddClusterBtn" );
        addClickListenerToAddNewNodeButton();
        controlsContent.addComponent( addNodeBtn );
        controlsContent.setComponentAlignment( addNodeBtn, Alignment.MIDDLE_CENTER );



        addStyleNameToButtons( refreshClustersBtn, checkAllBtn, startAllBtn, stopAllBtn, destroyClusterBtn, addNodeBtn, removeCluster );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private void addClickListenerToAddNewNodeButton()
    {
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add new node to the %s cluster?", config.getClusterName() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID track = cassandra.addNode( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, track,
                                    CassandraClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
    }


    private void addClickListenerToRemoveClusterButton()
    {
        removeCluster.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID track =  cassandra.removeCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, track,
                                    CassandraClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );
                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
    }


    private void addClickListenerToDestroyClusterButton()
    {
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            /** before destroying cluster, stop it first to not leave background zombie processes **/
                            stopAllBtn.click();
                            UUID trackID = cassandra.uninstallCluster( config.getClusterName() );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    CassandraClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
    }


    private void addClickListener( Button button )
    {
        if ( button.getCaption().equals( REFRESH_CLUSTERS_CAPTION ) )
        {
            button.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    refreshClustersInfo();
                }
            } );
            return;
        }
        switch ( button.getCaption() )
        {
            case CHECK_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            checkAllNodes();
                        }
                    }
                } );
                break;

            case START_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            startAllNodes();
                        }
                    }
                } );
                break;
            case STOP_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            stopAllNodes();
                        }
                    }
                } );
                break;
        }
    }


    private void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    private void disableOREnableAllButtonsOnTable( Table table, boolean value )
    {
        if ( table != null )
        {
            for ( Object o : table.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = table.getItem( rowId );
                HorizontalLayout availableOperationsLayout =
                        ( HorizontalLayout ) ( row.getItemProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION ).getValue() );
                if ( availableOperationsLayout != null )
                {
                    for ( Component component : availableOperationsLayout )
                    {
                        component.setEnabled( value );
                    }
                }
            }
        }
    }


    /**
     * Creates table in which all nodes in the cluster are listed.
     *
     * @param caption title of table
     *
     * @return nodes table
     */
    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( IP_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( NODE_ROLE_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( STATUS_COLUMN_CAPTION, Label.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        table.setColumnCollapsingAllowed( true );
        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String containerId =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( HOST_COLUMN_CAPTION )
                                            .getValue();
                    Set<ContainerHost> containerHosts =
                            environmentManager.getEnvironmentByUUID( config.getEnvironmentId() ).getContainerHosts();
                    Iterator iterator = containerHosts.iterator();
                    ContainerHost containerHost = null;
                    while ( iterator.hasNext() )
                    {
                        containerHost = ( ContainerHost ) iterator.next();
                        if ( containerHost.getId().equals( UUID.fromString( containerId ) ) )
                        {
                            break;
                        }
                    }
                    if ( containerHost != null )
                    {
                        TerminalWindow terminal = new TerminalWindow( containerHost );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Host not found" );
                    }
                }
            }
        } );
        return table;
    }


    /**
     * Shows notification with the given argument
     *
     * @param notification notification which will shown.
     */
    private void show( String notification )
    {
        Notification.show( notification );
    }


    /**
     * Fill out the table in which all nodes in the cluster are listed.
     *
     * @param table table to be filled
     */
    private void populateTable( final Table table, Set<ContainerHost> containerHosts )
    {
        table.removeAllItems();
        for ( final ContainerHost containerHost : containerHosts )
        {

            final Label resultHolder = new Label();
            resultHolder.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-cassandraResult" );
            final Button checkButton = new Button( CHECK_BUTTON_CAPTION );
            checkButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-cassandraCheck" );
            final Button startButton = new Button( START_BUTTON_CAPTION );
            startButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-cassandraStart" );
            final Button stopButton = new Button( STOP_BUTTON_CAPTION );
            stopButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-cassandraStop" );
            final Button destroyButton = new Button( DESTROY_NODE_BUTTON_CAPTION );
            destroyButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-cassandraStop" );

            addStyleNameToButtons( checkButton, startButton, stopButton, destroyButton );

            disableButtons( startButton, stopButton );
            PROGRESS_ICON.setVisible( false );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );

            String isSeed = checkIfSeed( containerHost.getId() );

            if ( ! isSeed.toLowerCase().equals( "seed" ) ){
                addGivenComponents( availableOperations, checkButton, startButton, stopButton, destroyButton );
            }
            else{
                addGivenComponents( availableOperations, checkButton, startButton, stopButton );
            }


            table.addItem( new Object[] {
                    containerHost.getHostname(), containerHost.getIpByInterfaceName( "eth0" ), isSeed,
                    resultHolder, availableOperations
            }, null );

            addClickListenerToCheckButton( containerHost, resultHolder, checkButton, startButton, stopButton, destroyButton );
            addClickListenerToStartButton( containerHost, checkButton, startButton, stopButton, destroyButton );
            addClickListenerToStopButton( containerHost, checkButton, startButton, stopButton, destroyButton );
            addClickListenerToDestroyButton( containerHost, checkButton, startButton, stopButton, destroyButton );
        }
    }


    private Button getButton( String caption, Button... buttons )
    {
        for ( Button b : buttons )
        {
            if ( b.getCaption().equals( caption ) )
            {
                return b;
            }
        }
        return null;
    }


    private void addClickListenerToDestroyButton( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( DESTROY_NODE_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy %s node ?", containerHost.getHostname() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID track = cassandra.destroyNode( config.getClusterName(), containerHost.getHostname() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, track,
                                    CassandraClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
    }


    private void addClickListenerToStopButton( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( cassandra, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.STOP, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    private void addClickListenerToStartButton( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( cassandra, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.START, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    private void addClickListenerToCheckButton( final ContainerHost containerHost, final Label resultHolder,
                                                final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( cassandra, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.STATUS, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    if ( nodeState.equals( NodeState.RUNNING ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }
                                    else if ( nodeState.equals( NodeState.STOPPED ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                    }
                                    else if ( nodeState.equals( NodeState.UNKNOWN ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }
                                    resultHolder.setValue( nodeState.name() );
                                    PROGRESS_ICON.setVisible( false );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    getButton( DESTROY_NODE_BUTTON_CAPTION, buttons ).setEnabled( true );
                                }
                            }
                        }, null ) );
            }
        } );
    }


    private void addGivenComponents( HorizontalLayout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    private void enableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


    private void stopAllNodes()
    {
        for ( UUID containerId : config.getNodes() )
        {
            ContainerHost containerHost = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() )
                                                            .getContainerHostById( containerId );
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new NodeOperationTask( cassandra, tracker, config.getClusterName(), containerHost,
                            NodeOperationType.STOP, new org.safehaus.subutai.common.protocol.CompleteEvent()
                    {
                        @Override
                        public void onComplete( NodeState nodeState )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodes();
                            }
                        }
                    }, null ) );
        }
    }


    private void startAllNodes()
    {
        for ( UUID containerId : config.getNodes() )
        {
            ContainerHost containerHost = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() )
                                                            .getContainerHostById( containerId );
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new NodeOperationTask( cassandra, tracker, config.getClusterName(), containerHost,
                            NodeOperationType.START, new org.safehaus.subutai.common.protocol.CompleteEvent()
                    {
                        @Override
                        public void onComplete( NodeState nodeState )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodes();
                            }
                        }
                    }, null ) );
        }
    }


    public void checkAllNodes()
    {
        if ( nodesTable != null )
        {
            for ( Object o : nodesTable.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = nodesTable.getItem( rowId );
                HorizontalLayout availableOperationsLayout =
                        ( HorizontalLayout ) ( row.getItemProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION ).getValue() );
                if ( availableOperationsLayout != null )
                {
                    Button checkBtn = getButton( availableOperationsLayout, CHECK_BUTTON_CAPTION );
                    if ( checkBtn != null )
                    {
                        checkBtn.click();
                    }
                }
            }
        }
    }


    protected Button getButton( final HorizontalLayout availableOperationsLayout, String caption )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( caption ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    /**
     * java.util.Set)}.
     */
    private void refreshUI()
    {
        if ( config != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
            Set<ContainerHost> containerHosts = new HashSet<>();
            for ( UUID uuid : config.getNodes() ){
                containerHosts.add( environment.getContainerHostById( uuid ) );
            }
            if ( environment != null )
            {
                populateTable( nodesTable, containerHosts );
            }
            else
            {
                Notification.show( String.format( "Could not get environment data" ) );
            }
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    /**
     * @param agentUUID agent
     *
     * @return Yes if give agent is among seeds, otherwise returns No
     */
    public String checkIfSeed( UUID agentUUID )
    {
        if ( config.getSeedNodes().contains( UUID.fromString( agentUUID.toString() ) ) )
        {
            return "Seed";
        }
        return "Not Seed";
    }


    /**
     * Refreshes combo box which lists available clusters in DB
     */
    public void refreshClustersInfo()
    {
        List<CassandraClusterConfig> info = cassandra.getClusters();
        if ( !info.isEmpty() )
        {
            CassandraClusterConfig clusterInfo = ( CassandraClusterConfig ) clusterCombo.getValue();
            clusterCombo.removeAllItems();
            for ( CassandraClusterConfig cassandraInfo : info )
            {
                clusterCombo.addItem( cassandraInfo );
                clusterCombo.setItemCaption( cassandraInfo, cassandraInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( CassandraClusterConfig cassandraInfo : info )
                {
                    if ( cassandraInfo.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( cassandraInfo );
                        return;
                    }
                }
            }
            else
            {
                clusterCombo.setValue( info.iterator().next() );
            }
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
