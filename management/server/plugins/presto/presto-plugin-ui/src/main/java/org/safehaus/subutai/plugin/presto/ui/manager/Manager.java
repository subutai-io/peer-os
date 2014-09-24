package org.safehaus.subutai.plugin.presto.ui.manager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
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

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final String COORDINATOR_PREFIX = "Coordinator: ";
    private final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final String message = "No cluster is installed !";
    private final ExecutorService executorService;
    private final Presto presto;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private PrestoClusterConfig config;

    protected final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected final static String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected final static String CHECK_BUTTON_CAPTION = "Check";
    protected final static String START_ALL_BUTTON_CAPTION = "Start All";
    protected final static String START_BUTTON_CAPTION = "Start";
    protected final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected final static String STOP_BUTTON_CAPTION = "Stop";
    protected final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected final static String DESTROY_BUTTON_CAPTION = "Destroy";
    protected final static String SET_AS_COORDINATOR_BUTTON_CAPTION = "Set As Coordinator";
    protected final static String HOST_COLUMN_CAPTION = "Host";
    protected final static String IP_COLUMN_CAPTION = "IP List";
    protected final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected final static String STATUS_COLUMN_CAPTION = "Status";
    protected final static String ADD_NODE_CAPTION = "Add Node";

    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.presto = serviceLocator.getService( Presto.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
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
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );

        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    checkAllNodes();
                }
            }
        } );
        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );

        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    startAllNodes();
                }
            }
        } );
        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );

        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    stopAllNodes();
                }
            }
        } );
        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );

        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.addStyleName( "default" );
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
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );

        addNodeBtn = new Button( ADD_NODE_CAPTION );
        addNodeBtn.addStyleName( "default" );
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( "Please, select cluster" );
                }

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
                        HashSet<Agent> nodes = new HashSet<>( info.getAllNodes() );
                        nodes.removeAll( config.getAllNodes() );
                        if ( !nodes.isEmpty() )
                        {
                            AddNodeWindow addNodeWindow =
                                    new AddNodeWindow( presto, executorService, tracker, config, nodes );
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
        } );

        controlsContent.addComponent( addNodeBtn );
        controlsContent.setComponentAlignment( addNodeBtn, Alignment.MIDDLE_CENTER );
        progressIcon.setVisible( false );
        controlsContent.addComponent( progressIcon );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private void populateTable( final Table table, Set<Agent> workers, final Agent coordinator )
    {

        table.removeAllItems();

        for ( final Agent agent : workers )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.addStyleName( "default" );
            final Button setCoordinatorBtn = new Button( SET_AS_COORDINATOR_BUTTON_CAPTION );
            setCoordinatorBtn.addStyleName( "default" );
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.addStyleName( "default" );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );


            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            availableOperations.addComponent( checkBtn );
            availableOperations.addComponent( startBtn );
            availableOperations.addComponent( stopBtn );
            availableOperations.addComponent( setCoordinatorBtn );
            availableOperations.addComponent( destroyBtn );


            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), checkIfCoordinator( agent ), resultHolder,
                    availableOperations
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    checkBtn.setEnabled( false );
                    setCoordinatorBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executorService.execute(
                            new CheckTask( presto, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                resultHolder.setValue( result );
                                                if ( result.contains( "Not" ) )
                                                {
                                                    startBtn.setEnabled( true );
                                                    stopBtn.setEnabled( false );
                                                }
                                                else
                                                {
                                                    startBtn.setEnabled( false );
                                                    stopBtn.setEnabled( true );
                                                }
                                                progressIcon.setVisible( false );
                                                destroyBtn.setEnabled( true );
                                                setCoordinatorBtn.setEnabled( true );
                                                checkBtn.setEnabled( true );
                                            }
                                        }
                                    } ) );
                }
            } );

            startBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    setCoordinatorBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executorService.execute(
                            new StartTask( presto, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                checkBtn.click();
                                            }
                                        }
                                    } ) );
                }
            } );

            stopBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    setCoordinatorBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executorService.execute(
                            new StopTask( presto, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                checkBtn.click();
                                            }
                                        }
                                    } ) );
                }
            } );

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
                            UUID trackID = presto.changeCoordinatorNode( config.getClusterName(), agent.getHostname() );
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

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s node?", agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = presto.destroyWorkerNode( config.getClusterName(), agent.getHostname() );
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

        //add coordinator here
        final Label resultHolder = new Label();
        final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
        checkBtn.addStyleName( "default" );
        final Button startBtn = new Button( START_BUTTON_CAPTION );
        startBtn.addStyleName( "default" );
        final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
        stopBtn.addStyleName( "default" );

        stopBtn.setEnabled( false );
        startBtn.setEnabled( false );
        progressIcon.setVisible( false );

        HorizontalLayout availableOperations = new HorizontalLayout();
        availableOperations.setSpacing( true );
        availableOperations.addStyleName( "default" );

        availableOperations.addComponent( checkBtn );
        availableOperations.addComponent( startBtn );
        availableOperations.addComponent( stopBtn );

        table.addItem( new Object[] {
                coordinator.getHostname(), coordinator.getListIP().get( 0 ), checkIfCoordinator( coordinator ),
                resultHolder, availableOperations
        }, null );

        checkBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );
                checkBtn.setEnabled( false );

                executorService.execute(
                        new CheckTask( presto, tracker, config.getClusterName(), coordinator.getHostname(),
                                new CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( String result )
                                    {
                                        synchronized ( progressIcon )
                                        {
                                            resultHolder.setValue( result );
                                            if ( result.contains( "Not" ) )
                                            {
                                                startBtn.setEnabled( true );
                                                stopBtn.setEnabled( false );
                                            }
                                            else
                                            {
                                                startBtn.setEnabled( false );
                                                stopBtn.setEnabled( true );
                                            }
                                            progressIcon.setVisible( false );
                                            checkBtn.setEnabled( true );
                                        }
                                    }
                                } ) );
            }
        } );

        startBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                executorService.execute(
                        new StartTask( presto, tracker, config.getClusterName(), coordinator.getHostname(),
                                new CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( String result )
                                    {
                                        synchronized ( progressIcon )
                                        {
                                            checkBtn.click();
                                        }
                                    }
                                } ) );
            }
        } );

        stopBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                executorService.execute(
                        new StopTask( presto, tracker, config.getClusterName(), coordinator.getHostname(),
                                new CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( String result )
                                    {
                                        synchronized ( progressIcon )
                                        {
                                            checkBtn.click();
                                        }
                                    }
                                } ) );
            }
        } );
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
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent =
                            agentManager.getAgentByHostname( lxcHostname.replaceAll( COORDINATOR_PREFIX, "" ) );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executorService, commandRunner,
                                        agentManager );
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


    /**
     * @param agent agent
     *
     * @return Yes if give agent is among seeds, otherwise returns No
     */
    public String checkIfCoordinator( Agent agent )
    {
        if ( config.getCoordinatorNode().equals( agent ) )
        {
            return "Coordinator";
        }
        return "Worker";
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( nodesTable, config.getWorkers(), config.getCoordinatorNode() );
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
        for ( Agent agent : config.getAllNodes() )
        {
            progressIcon.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new StartTask( presto, tracker, config.getClusterName(), agent.getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodes();
                            }
                        }
                    } ) );
        }
    }


    public void stopAllNodes()
    {
        for ( Agent agent : config.getAllNodes() )
        {
            progressIcon.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new StopTask( presto, tracker, config.getClusterName(), agent.getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodes();
                            }
                        }
                    } ) );
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
