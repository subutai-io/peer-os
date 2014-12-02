package org.safehaus.subutai.plugin.spark.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
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
    final Button refreshClustersBtn, startAllNodesBtn, stopAllNodesBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ExecutorService executor;
    private final Spark spark;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final EnvironmentManager environmentManager;
    private final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private SparkClusterConfig config;
    private Environment environment;


    public Manager( final ExecutorService executor, Spark spark, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        Preconditions.checkNotNull( executor, "Executor is null" );

        this.spark = spark;
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.environmentManager = environmentManager;

        this.executor = executor;
        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "sparkNodesTable" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "sparkClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( SparkClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodesStatus();
            }
        } );

        controlsContent.addComponent( clusterCombo );


        /** Refresh Cluster button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        addClickListener( refreshClustersBtn );
        controlsContent.addComponent( refreshClustersBtn );


        /** Check All button */
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        addClickListener( checkAllBtn );
        controlsContent.addComponent( checkAllBtn );


        /** Start all button */
        startAllNodesBtn = new Button( START_ALL_BUTTON_CAPTION );
        addClickListener( startAllNodesBtn );
        controlsContent.addComponent( startAllNodesBtn );


        /** Stop all button */
        stopAllNodesBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        addClickListener( stopAllNodesBtn );
        controlsContent.addComponent( stopAllNodesBtn );


        /** Destroy Cluster button */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        addClickListenerToDestroyClusterButton();
        controlsContent.addComponent( destroyClusterBtn );


        /** Add Node button */
        addNodeBtn = new Button( ADD_NODE_CAPTION );
        addClickListenerToAddNodeButton();
        controlsContent.addComponent( addNodeBtn );

        addStyleNameToButtons( refreshClustersBtn, checkAllBtn, startAllNodesBtn, stopAllNodesBtn, destroyClusterBtn,
                addNodeBtn );

        progressIcon.setVisible( false );
        controlsContent.addComponent( progressIcon );

        refreshClustersBtn.setId( "sparkRefresh" );
        checkAllBtn.setId( "sparkCheckAll" );
        startAllNodesBtn.setId( "sparkStartAll" );
        stopAllNodesBtn.setId( "sparkStopAll" );
        destroyClusterBtn.setId( "sparkDestroyCluster" );
        addNodeBtn.setId( "sparkAddNode" );
        progressIcon.setId( "indicator" );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
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
                    show( "Select cluster" );
                    return;
                }

                Set<ContainerHost> set = null;

                String hn = config.getHadoopClusterName();
                if ( !Strings.isNullOrEmpty( hn ) )
                {
                    HadoopClusterConfig hci = hadoop.getCluster( hn );
                    if ( hci != null )
                    {
                        set = environment.getHostsByIds( Sets.newHashSet( hci.getAllNodes() ) );
                    }
                }


                if ( set == null )
                {
                    show( "Hadoop cluster not found" );
                    return;
                }
                set.removeAll( environment.getHostsByIds( Sets.newHashSet( config.getAllNodesIds() ) ) );
                if ( set.isEmpty() )
                {
                    show( "All nodes in Hadoop cluster have Spark installed" );
                    return;
                }

                AddNodeWindow w = new AddNodeWindow( executor, spark, tracker, config, set );
                contentRoot.getUI().addWindow( w );
                w.addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                    }
                } );
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
                            stopAllNodesBtn.click();
                            UUID trackID = spark.uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( executor, tracker, trackID, SparkClusterConfig.PRODUCT_KEY );
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


    public void addClickListener( final Button button )
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
                            checkAllNodesStatus();
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
                    String hostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    ContainerHost node = environment.getContainerHostByHostname( hostname );
                    if ( node != null )
                    {
                        TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( node ) );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Container not found" );
                    }
                }
            }
        } );
        return table;
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );

            populateTable( nodesTable, environment.getHostsByIds( config.getSlaveIds() ),
                    environment.getContainerHostByUUID( config.getMasterNodeId() ) );
            checkAllNodesStatus();
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        List<SparkClusterConfig> clustersInfo = spark.getClusters();
        SparkClusterConfig clusterInfo = ( SparkClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( SparkClusterConfig ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                clusterCombo.setItemCaption( ci, ci.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( SparkClusterConfig mongoClusterInfo : clustersInfo )
                {
                    if ( mongoClusterInfo.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( mongoClusterInfo );
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


    public String checkIfMaster( ContainerHost node )
    {
        if ( config.getMasterNodeId().equals( node.getId() ) )
        {
            return "Master";
        }
        return "Slave";
    }


    protected Button getButton( final HorizontalLayout availableOperationsLayout, String buttonCaption )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( buttonCaption ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    private void startAllNodes()
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
                    Button checkBtn = getButton( availableOperationsLayout, START_BUTTON_CAPTION );
                    if ( checkBtn != null )
                    {
                        checkBtn.click();
                    }
                }
            }
        }
    }


    private void stopAllNodes()
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
                    Button checkBtn = getButton( availableOperationsLayout, STOP_BUTTON_CAPTION );
                    if ( checkBtn != null )
                    {
                        checkBtn.click();
                    }
                }
            }
        }
    }


    public void checkAllNodesStatus()
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


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void populateTable( final Table table, Set<ContainerHost> slaves, final ContainerHost master )
    {

        table.removeAllItems();

        for ( final ContainerHost node : slaves )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );

            checkBtn.setId( node.getIpByInterfaceName( "eth0" ) + "-sparkCheck" );
            startBtn.setId( node.getIpByInterfaceName( "eth0" ) + "-sparkStart" );
            stopBtn.setId( node.getIpByInterfaceName( "eth0" ) + "-sparkStop" );
            destroyBtn.setId( node.getIpByInterfaceName( "eth0" ) + "-sparkDestroy" );

            addStyleNameToButtons( checkBtn, startBtn, stopBtn, destroyBtn );
            enableButtons( startBtn, stopBtn );
            progressIcon.setVisible( false );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );

            addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn, destroyBtn );

            table.addItem( new Object[] {
                    node.getHostname(), node.getIpByInterfaceName( "eth0" ), checkIfMaster( node ), resultHolder,
                    availableOperations
            }, null );


            addClickListenerToSlaveCheckButton( node, resultHolder, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToStartButton( node, false, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToStopButton( node, false, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToDestroyButton( node, destroyBtn );
        }

        //add master here
        final Label resultHolder = new Label();
        final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
        final Button startBtn = new Button( START_BUTTON_CAPTION );
        final Button stopBtn = new Button( STOP_BUTTON_CAPTION );


        checkBtn.setId( master.getIpByInterfaceName( "eth0" ) + "-sparkCheck" );
        startBtn.setId( master.getIpByInterfaceName( "eth0" ) + "-sparkStart" );
        stopBtn.setId( master.getIpByInterfaceName( "eth0" ) + "-sparkStop" );

        addStyleNameToButtons( checkBtn, startBtn, stopBtn );

        disableButtons( stopBtn, startBtn );
        progressIcon.setVisible( false );

        final HorizontalLayout availableOperations = new HorizontalLayout();
        availableOperations.addStyleName( "default" );
        availableOperations.setSpacing( true );

        addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn );

        table.addItem( new Object[] {
                master.getHostname(), master.getIpByInterfaceName( "eth0" ), checkIfMaster( master ), resultHolder,
                availableOperations
        }, null );

        addClickListenerToMasterCheckButton( master, resultHolder, checkBtn, startBtn, stopBtn );
        addClickListenerToStartButton( master, true, checkBtn, startBtn, stopBtn );
        addClickListenerToStopButton( master, true, checkBtn, startBtn, stopBtn );
    }


    public void addClickListenerToSlaveCheckButton( final ContainerHost node, final Label resultHolder,
                                                    final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                disableButtons( buttons );

                executor.execute( new CheckNodeTask( spark, tracker, config.getClusterName(), node.getHostname(),
                        new CompleteEvent()
                        {
                            @Override
                            public void onComplete( String result )
                            {
                                synchronized ( progressIcon )
                                {
                                    resultHolder.setValue( result );
                                    if ( result.contains( "NOT" ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                    }
                                    else
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }
                                    progressIcon.setVisible( false );
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
                        }, false ) );
            }
        } );
    }


    public void addClickListenerToMasterCheckButton( final ContainerHost node, final Label resultHolder,
                                                     final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                disableButtons( buttons );

                executor.execute( new CheckNodeTask( spark, tracker, config.getClusterName(), node.getHostname(),
                        new CompleteEvent()
                        {
                            @Override
                            public void onComplete( String result )
                            {
                                synchronized ( progressIcon )
                                {
                                    resultHolder.setValue( result );
                                    if ( result.contains( "NOT" ) )
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                    }
                                    else
                                    {
                                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                    }
                                    progressIcon.setVisible( false );
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
                        }, true ) );
            }
        } );
    }


    public void addClickListenerToStartButton( final ContainerHost node, final boolean isMaster,
                                               final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                disableButtons( buttons );
                executor.execute( new StartTask( spark, tracker, config.getClusterName(), node.getHostname(), isMaster,
                        new CompleteEvent()
                        {
                            @Override
                            public void onComplete( String result )
                            {
                                synchronized ( progressIcon )
                                {
                                    enableButtons( getButton( CHECK_BUTTON_CAPTION, buttons ) );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        } ) );
            }
        } );
    }


    public void addClickListenerToDestroyButton( final ContainerHost node, final Button... buttons )
    {
        getButton( DESTROY_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
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
                        UUID trackID = spark.destroySlaveNode( config.getClusterName(), node.getHostname() );
                        ProgressWindow window =
                                new ProgressWindow( executor, tracker, trackID, SparkClusterConfig.PRODUCT_KEY );
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


    public void addClickListenerToStopButton( final ContainerHost node, final boolean isMaster,
                                              final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                disableButtons( buttons );
                executor.execute( new StopTask( spark, tracker, config.getClusterName(), node.getHostname(), isMaster,
                        new CompleteEvent()
                        {
                            @Override
                            public void onComplete( String result )
                            {
                                synchronized ( progressIcon )
                                {
                                    enableButtons( getButton( CHECK_BUTTON_CAPTION, buttons ) );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        } ) );
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


    public Component getContent()
    {
        return contentRoot;
    }
}
