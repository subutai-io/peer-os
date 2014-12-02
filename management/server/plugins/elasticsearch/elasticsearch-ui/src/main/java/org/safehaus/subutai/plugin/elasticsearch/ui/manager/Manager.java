package org.safehaus.subutai.plugin.elasticsearch.ui.manager;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.api.NodeOperationTask;
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
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String BUTTON_STYLE_NAME = "default";
    private static final String MESSAGE = "No cluster is installed !";
    private static final Pattern ELASTICSEARCH_PATTERN = Pattern.compile( ".*(elasticsearch.+?g).*" );
    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final Table nodesTable;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final Elasticsearch elasticsearch;
    private final EnvironmentManager environmentManager;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private ElasticsearchClusterConfiguration config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.elasticsearch = serviceLocator.getService( Elasticsearch.class );
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );


        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Cluster nodes" );
        nodesTable.setId( "ElasticSearchMngNodesTable" );
        contentRoot.setId( "ElasticSearchMngContentRoot" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );
        controlsContent.setHeight( 100, Sizeable.Unit.PERCENTAGE );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
        controlsContent.setComponentAlignment( clusterNameLabel, Alignment.MIDDLE_CENTER );


        /**  Combo box  */
        clusterCombo = new ComboBox();
        clusterCombo.setId( "ElasticSearchMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( ElasticsearchClusterConfiguration ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );

        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );

        /**  Refresh clusters button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "ElasticSearchMngRefresh" );
        addClickListener( refreshClustersBtn );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        /** Check all button */
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "ElasticSearchMngCheckAll" );
        addClickListener( checkAllBtn );
        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        /**  Start all button */
        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.setId( "ElasticSearchMngStartAll" );
        addClickListener( startAllBtn );
        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );


        /**  Stop all button  */
        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.setId( "ElasticSearchMngStopAll" );
        addClickListener( stopAllBtn );
        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );


        /**  Destroy cluster button  */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "ElasticSearchMngDestroyCuster" );
        addClickListenerToDestroyClusterButton();
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );


        /**  Add Node button  */
        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId( "ZookeeperMngAddNode" );
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


    private void addClickListenerToAddNodeButton()
    {
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( config != null )
                {

                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add node to the %s cluster?", config.getClusterName() ),
                            "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = elasticsearch.addNode( config.getClusterName(), null );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    ElasticsearchClusterConfiguration.PRODUCT_KEY );
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
                            UUID trackID = elasticsearch.uninstallCluster( config );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    ElasticsearchClusterConfiguration.PRODUCT_KEY );

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


    public void startAllNodes()
    {
        for ( UUID containerUUID : config.getNodes() )
        {
            ContainerHost containerHost = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() )
                                                            .getContainerHostByUUID( containerUUID );
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new NodeOperationTask( elasticsearch, tracker, config.getClusterName(), containerHost,
                            NodeOperationType.START, new CompleteEvent()
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


    private void stopAllNodes()
    {
        for ( UUID containerUUID : config.getNodes() )
        {
            ContainerHost containerHost = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() )
                                                            .getContainerHostByUUID( containerUUID );
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new NodeOperationTask( elasticsearch, tracker, config.getClusterName(), containerHost,
                            NodeOperationType.STOP, new CompleteEvent()
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


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( IP_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( STATUS_COLUMN_CAPTION, Label.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        table.setColumnCollapsingAllowed( true );

        addItemClickListenerToTable( table );
        return table;
    }


    public void addItemClickListenerToTable( final Table table )
    {
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
                            environmentManager.getEnvironmentByUUID( config.getEnvironmentId() ).getContainers();
                    Iterator iterator = containerHosts.iterator();
                    ContainerHost containerHost = null;
                    while ( iterator.hasNext() )
                    {
                        containerHost = ( ContainerHost ) iterator.next();
                        if ( containerHost.getHostname().equals( containerId ) )
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
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
            populateTable( nodesTable, environment.getContainers() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        List<ElasticsearchClusterConfiguration> clusters = elasticsearch.getClusters();
        ElasticsearchClusterConfiguration clusterInfo = ( ElasticsearchClusterConfiguration ) clusterCombo.getValue();
        clusterCombo.removeAllItems();

        if ( clusters == null || clusters.isEmpty() )
        {
            PROGRESS_ICON.setVisible( false );
            return;
        }

        for ( ElasticsearchClusterConfiguration esConfig : clusters )
        {
            clusterCombo.addItem( esConfig );
            clusterCombo.setItemCaption( esConfig, esConfig.getClusterName() );
        }

        if ( clusterInfo != null )
        {
            for ( ElasticsearchClusterConfiguration esConfig : clusters )
            {
                if ( esConfig.getClusterName().equals( clusterInfo.getClusterName() ) )
                {
                    clusterCombo.setValue( esConfig );
                    return;
                }
            }
        }
        else
        {
            clusterCombo.setValue( clusters.iterator().next() );
        }
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


    public void addGivenComponents( HorizontalLayout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    /**
     * Fill out the table in which all nodes in the cluster are listed.
     *
     * @param table table to be filled
     * @param containerHosts nodes
     */
    private void populateTable( final Table table, Set<ContainerHost> containerHosts )
    {
        table.removeAllItems();
        for ( final ContainerHost containerHost : containerHosts )
        {
            final Label resultHolder = new Label();
            final Button checkButton = new Button( CHECK_BUTTON_CAPTION );
            checkButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-elasticsearchCheck" );
            final Button startButton = new Button( START_BUTTON_CAPTION );
            startButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-elasticsearchStart" );
            final Button stopButton = new Button( STOP_BUTTON_CAPTION );
            stopButton.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-elasticsearchStop" );
            final Button destroyButton = new Button( DESTROY_BUTTON_CAPTION );


            addStyleNameToButtons( checkButton, startButton, stopButton, destroyButton );
            enableButtons( startButton, stopButton );
            PROGRESS_ICON.setVisible( false );
            PROGRESS_ICON.setId( "indicator" );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            addGivenComponents( availableOperations, checkButton, startButton, stopButton, destroyButton );

            table.addItem( new Object[] {
                    containerHost.getHostname(), containerHost.getIpByInterfaceName( "eth0" ), resultHolder,
                    availableOperations
            }, null );

            addCheckButtonClickListener( containerHost, resultHolder, checkButton, startButton, stopButton,
                    destroyButton );
            addStartButtonClickListener( containerHost, checkButton, startButton, stopButton, destroyButton );
            addStopButtonClickListener( containerHost, checkButton, startButton, stopButton, destroyButton );
            addDestroyButtonClickListener( containerHost, checkButton, startButton, stopButton, destroyButton );
        }
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


    private void addDestroyButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( DESTROY_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", containerHost.getHostname() ), "Yes",
                        "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID =
                                elasticsearch.destroyNode( config.getClusterName(), containerHost.getHostname() );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                ElasticsearchClusterConfiguration.PRODUCT_KEY );
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


    public void addStopButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( elasticsearch, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.STOP, new CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    enableButtons( buttons );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addStartButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( elasticsearch, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.START, new CompleteEvent()
                        {
                            @Override
                            public void onComplete( NodeState nodeState )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    enableButtons( buttons );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addCheckButtonClickListener( final ContainerHost containerHost, final Label resultHolder,
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
                        new NodeOperationTask( elasticsearch, tracker, config.getClusterName(), containerHost,
                                NodeOperationType.STATUS, new CompleteEvent()
                        {
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
                                    getButton( DESTROY_BUTTON_CAPTION, buttons ).setEnabled( true );
                                }
                            }
                        }, null ) );
            }
        } );
    }


    public void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
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


    public Component getContent()
    {
        return contentRoot;
    }
}