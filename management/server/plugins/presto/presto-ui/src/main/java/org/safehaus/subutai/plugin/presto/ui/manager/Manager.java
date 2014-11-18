package org.safehaus.subutai.plugin.presto.ui.manager;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.NodeOperationTask;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
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
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String ADD_NODE_CAPTION = "Add Node";
    protected static final String BUTTON_STYLE_NAME = "default";
    private static final String MESSAGE = "No cluster is installed !";
    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final ExecutorService executorService;
    private final Presto presto;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private PrestoClusterConfig config;
    private final EnvironmentManager environmentManager;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.presto = serviceLocator.getService( Presto.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "PrestoNodesTable" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "PresClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( PrestoClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );
        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );


        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "PresRefreshClustersBtn" );
        addClickListener( refreshClustersBtn );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "PresCheckAll" );
        addClickListener( checkAllBtn );
        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.setId( "PresStartAll" );
        addClickListener( startAllBtn );
        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );


        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.setId( "PresStopAll" );
        addClickListener( stopAllBtn );
        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );


        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "PresDestroyClusters" );
        addClickListenerToDestroyClusterButton();
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );


        addNodeBtn = new Button( ADD_NODE_CAPTION );
        addNodeBtn.setId( "PresAddNode" );
        addClickListenerToAddNodeButton();
        controlsContent.addComponent( addNodeBtn );
        controlsContent.setComponentAlignment( addNodeBtn, Alignment.MIDDLE_CENTER );

        addStyleNameToButtons( refreshClustersBtn, checkAllBtn, startAllBtn, stopAllBtn, destroyClusterBtn,
                addNodeBtn );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    public void addClickListener( Button button )
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


    public void addClickListenerToAddNodeButton()
    {
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( "Please, select cluster" );
                }
                else
                {
                    Set<ContainerHost> set = null;
                    if ( config.getSetupType() == SetupType.OVER_HADOOP )
                    {
                        String hn = config.getHadoopClusterName();
                        if ( hn == null || hn.isEmpty() )
                        {
                            show( "Undefined Hadoop cluster name" );
                            return;
                        }
                        HadoopClusterConfig info = hadoop.getCluster( hn );
                        if ( info != null )
                        {
                            set = environmentManager.getEnvironmentByUUID( info.getEnvironmentId() ).getHostsByIds( info.getAllNodes() );
                            set.removeAll( config.getAllNodes() );
                            if ( !set.isEmpty() )
                            {
                                AddNodeWindow addNodeWindow =
                                        new AddNodeWindow( presto, executorService, tracker, config, set );
                                contentRoot.getUI().addWindow( addNodeWindow );
                                addNodeWindow.addCloseListener( new Window.CloseListener()
                                {
                                    @Override
                                    public void windowClose( Window.CloseEvent closeEvent )
                                    {
                                        refreshClustersInfo();
                                    }
                                } );
                            }
                            else
                            {
                                show( "All nodes in corresponding Hadoop cluster have Presto installed" );
                            }
                        }
                        else
                        {
                            show( "Hadoop cluster info not found" );
                        }
                    }
                    else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                    {
                        ConfirmationDialog d = new ConfirmationDialog( "Add node to cluster", "OK", "Cancel" );
                        d.getOk().addClickListener( new Button.ClickListener()
                        {

                            @Override
                            public void buttonClick( Button.ClickEvent event )
                            {
                                UUID trackId = presto.addWorkerNode( config.getClusterName(), null );
                                ProgressWindow w = new ProgressWindow( executorService, tracker, trackId,
                                        PrestoClusterConfig.PRODUCT_KEY );
                                contentRoot.getUI().addWindow( w.getWindow() );
                            }
                        } );
                        contentRoot.getUI().addWindow( d.getAlert() );
                    }
                }
            }
        } );
    }


    public void addClickListenerToDestroyClusterButton()
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
                            UUID trackID = presto.uninstallCluster( config );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    PrestoClusterConfig.PRODUCT_KEY );

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


    private void populateTable( final Table table, Set<ContainerHost> workers, final ContainerHost coordinator )
    {
        table.removeAllItems();

        for ( final ContainerHost node : workers )
        {
            final Label resultHolder = new Label();
            resultHolder.setId( node.getAgent().getListIP().get( 0 ) + "-prestoResult" );
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.setId( node.getAgent().getListIP().get( 0 ) + "-prestoCheck" );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.setId( node.getAgent().getListIP().get( 0 ) + "-prestoStart" );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.setId( node.getAgent().getListIP().get( 0 ) + "-prestoStop" );


            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.setId( node.getAgent().getListIP().get( 0 ) + "-prestoDestroy" );

            addStyleNameToButtons( checkBtn, startBtn, stopBtn, destroyBtn );
            disableButtons( startBtn, stopBtn );
            PROGRESS_ICON.setVisible( false );
            PROGRESS_ICON.setId( "indicator" );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( BUTTON_STYLE_NAME );

            addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn, destroyBtn );

            table.addItem( new Object[] {
                    node.getHostname(), node.getAgent().getListIP().get( 0 ), checkIfCoordinator( node ), resultHolder,
                    availableOperations
            }, null );

            /** add click listeners to button */
            addClickListenerToSlavesCheckButton( node, resultHolder, checkBtn, startBtn, stopBtn, destroyBtn );
            addClickListenerToStartButtons( node, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToStopButtons( node, startBtn, stopBtn, checkBtn );
            addClickListenerToDestroyButton( node, destroyBtn );
        }

        /** add Coordinator here */
        final Label resultHolder = new Label();
        resultHolder.setId( coordinator.getAgent().getListIP().get( 0 ) + "-prestoResult" );
        final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
        checkBtn.setId( coordinator.getAgent().getListIP().get( 0 ) + "-prestoCheck" );
        final Button startBtn = new Button( START_BUTTON_CAPTION );
        startBtn.setId( coordinator.getAgent().getListIP().get( 0 ) + "-prestoStart" );
        final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
        stopBtn.setId( coordinator.getAgent().getListIP().get( 0 ) + "-prestoStop" );

        addStyleNameToButtons( checkBtn, startBtn, stopBtn );

        disableButtons( startBtn, stopBtn );
        PROGRESS_ICON.setVisible( false );

        HorizontalLayout availableOperations = new HorizontalLayout();
        availableOperations.setSpacing( true );
        availableOperations.addStyleName( BUTTON_STYLE_NAME );

        addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn );

        table.addItem( new Object[] {
                coordinator.getHostname(), coordinator.getAgent().getListIP().get( 0 ), checkIfCoordinator( coordinator ),
                resultHolder, availableOperations
        }, null );

        addClickListenerToMasterCheckButton( coordinator, resultHolder, checkBtn, startBtn, stopBtn );
        addClickListenerToStartButtons( coordinator, startBtn, stopBtn, checkBtn );
        addClickListenerToStopButtons( coordinator, startBtn, stopBtn, checkBtn );
    }


    public void addGivenComponents( HorizontalLayout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    public void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    public void addClickListenerToSetCoordinatorButton( final Agent agent, Button setCoordinatorBtn )
    {
        setCoordinatorBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to set %s as coordinator node?", agent.getHostname() ), "Yes",
                        "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID = presto.uninstallCluster( config.getClusterName() );

                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                PrestoClusterConfig.PRODUCT_KEY );

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
        } );
    }


    public void addClickListenerToDestroyButton( final ContainerHost node, Button destroyBtn )
    {
        destroyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", node.getHostname() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID = presto.destroyWorkerNode( config.getClusterName(), node.getHostname() );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                PrestoClusterConfig.PRODUCT_KEY );
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
        } );
    }


    public void addClickListenerToStartButtons( final ContainerHost host, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( presto, tracker, config.getClusterName(), host,
                                NodeOperationType.START, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    enableButtons( getButton( CHECK_BUTTON_CAPTION, buttons ) );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addClickListenerToStopButtons( final ContainerHost host, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( presto, tracker, config.getClusterName(), host,
                                NodeOperationType.STOP, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    enableButtons( getButton( CHECK_BUTTON_CAPTION, buttons ) );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addClickListenerToMasterCheckButton( final ContainerHost coordinator, final Label resultHolder,
                                                     final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( presto, tracker, config.getClusterName(), coordinator,
                                NodeOperationType.STATUS, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    resultHolder.setValue( nodeState.name() );
                                    if ( nodeState.name().contains( "STOPPED" )  )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                    }
                                    else
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }

                                    PROGRESS_ICON.setVisible( false );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addClickListenerToSlavesCheckButton( final ContainerHost host, final Label resultHolder,
                                                     final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( presto, tracker, config.getClusterName(), host,
                                NodeOperationType.STATUS, new org.safehaus.subutai.common.protocol.CompleteEvent()
                        {
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    resultHolder.setValue( nodeState.name() );
                                    if ( nodeState.name().contains( "STOPPED" ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                    }
                                    else
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }

                                    PROGRESS_ICON.setVisible( false );
                                    for ( Button b : buttons )
                                    {
                                        if ( b.getCaption().equals( CHECK_BUTTON_CAPTION ) || b.getCaption().equals(
                                                DESTROY_BUTTON_CAPTION ) )
                                        {
                                            enableButtons( b );
                                        }
                                    }
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public Button getButton( String caption, Button... buttons )
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


    public void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    public void enableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


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
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Set<ContainerHost> containerHosts =
                            environmentManager.getEnvironmentByUUID( config.getEnvironmentId() ).getContainers();
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
                        TerminalWindow terminal = new TerminalWindow( containerHosts );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    public void disableOREnableAllButtonsOnTable( Table table, boolean value )
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

    public String checkIfCoordinator( ContainerHost node )
    {
        if ( config.getCoordinatorNode().equals( node.getId() ) )
        {
            return "Coordinator";
        }
        return "Worker";
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
            populateTable( nodesTable, environment.getHostsByIds( config.getWorkers() ), environment.getContainerHostByUUID( config.getCoordinatorNode() ) );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        List<PrestoClusterConfig> clustersInfo = presto.getClusters();
        PrestoClusterConfig clusterInfo = ( PrestoClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( PrestoClusterConfig c : clustersInfo )
            {
                clusterCombo.addItem( c );
                clusterCombo.setItemCaption( c, c.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( PrestoClusterConfig c : clustersInfo )
                {
                    if ( c.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( c );
                        return;
                    }
                }
            }
            else
            {
                clusterCombo.setValue( clustersInfo.iterator().next() );
            }
        }
    }


    public void startAllNodes()
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
                    Button startBtn = getButton( availableOperationsLayout, START_BUTTON_CAPTION );
                    if ( startBtn != null )
                    {
                        startBtn.click();
                    }
                }
            }
        }
    }


    public void stopAllNodes()
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
                    Button stopBtn = getButton( availableOperationsLayout, STOP_BUTTON_CAPTION );
                    if ( stopBtn != null )
                    {
                        stopBtn.click();
                    }
                }
            }
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


    private void show( String notification )
    {
        Notification.show( notification );
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


    public Component getContent()
    {
        return contentRoot;
    }
}
