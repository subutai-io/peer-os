package org.safehaus.subutai.plugin.spark.ui.manager;


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
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.base.Preconditions;
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

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ExecutorService executor;
    private final Spark spark;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final String message = "No cluster is installed !";
    private final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private SparkClusterConfig config;

    protected final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected final static String CHECK_BUTTON_CAPTION = "Check";
    protected final static String START_ALL_BUTTON_CAPTION = "Start All";
    protected final static String START_BUTTON_CAPTION = "Start";
    protected final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected final static String STOP_BUTTON_CAPTION = "Stop";
    protected final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected final static String DESTROY_BUTTON_CAPTION = "Destroy";
    protected final static String HOST_COLUMN_CAPTION = "Host";
    protected final static String IP_COLUMN_CAPTION = "IP List";
    protected final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected final static String STATUS_COLUMN_CAPTION = "Status";
    protected final static String ADD_NODE_CAPTION = "Add Node";

    final Button refreshClustersBtn, startAllNodesBtn, stopAllNodesBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;


    public Manager( final ExecutorService executor, final ServiceLocator serviceLocator ) throws NamingException
    {
        Preconditions.checkNotNull( executor, "Executor is null" );
        Preconditions.checkNotNull( serviceLocator, "Service Locator is null" );

        this.spark = serviceLocator.getService( Spark.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );

        this.executor = executor;
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
                config = ( SparkClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodesStatus();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.addStyleName( "default" );

        startAllNodesBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllNodesBtn.addStyleName( "default" );

        stopAllNodesBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllNodesBtn.addStyleName( "default" );

        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.addStyleName( "default" );

        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.addStyleName( "default" );

        addNodeBtn = new Button( ADD_NODE_CAPTION );
        addNodeBtn.addStyleName( "default" );


        /** Refresh Cluster button */
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );

        /** Check All button */
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
                    checkAllNodesStatus();
                }
            }
        } );
        controlsContent.addComponent( checkAllBtn );

        /** Start all button */
        startAllNodesBtn.addClickListener( new Button.ClickListener()
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
                    progressIcon.setVisible( true );
                    disableOREnableAllTopButtons( false );
                    disableOREnableAllButtonsOnTable( nodesTable, false );
                    executor.execute( new StartAllTask( spark, tracker, config.getClusterName(),
                            config.getMasterNode().getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodesStatus();
                                disableOREnableAllTopButtons( true );
                            }
                        }
                    } ) );
                }
            }
        } );
        controlsContent.addComponent( startAllNodesBtn );

        /** Stop all button */
        stopAllNodesBtn.addClickListener( new Button.ClickListener()
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
                    progressIcon.setVisible( true );
                    disableOREnableAllTopButtons( false );
                    disableOREnableAllButtonsOnTable( nodesTable, false );
                    executor.execute( new StopAllTask( spark, tracker, config.getClusterName(),
                            config.getMasterNode().getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkAllNodesStatus();
                                disableOREnableAllTopButtons( true );
                            }
                        }
                    } ) );
                }
            }
        } );
        controlsContent.addComponent( stopAllNodesBtn );


        /** Destroy Cluster button */
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
        controlsContent.addComponent( destroyClusterBtn );


        /** Add Node button */
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

                Set<Agent> set = null;
                if ( config.getSetupType() == SetupType.OVER_HADOOP )
                {
                    String hn = config.getHadoopClusterName();
                    if ( hn != null && !hn.isEmpty() )
                    {
                        HadoopClusterConfig hci = hadoop.getCluster( hn );
                        if ( hci != null )
                        {
                            set = new HashSet<>( hci.getAllNodes() );
                        }
                    }
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                {
                    set = new HashSet<>( config.getHadoopNodes() );
                }

                if ( set == null )
                {
                    show( "Hadoop cluster not found" );
                    return;
                }
                set.removeAll( config.getAllNodes() );
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

        controlsContent.addComponent( addNodeBtn );
        controlsContent.addComponent( progressIcon );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    public void disableOREnableAllTopButtons( boolean value )
    {
        refreshClustersBtn.setEnabled( value );
        startAllNodesBtn.setEnabled( value );
        stopAllNodesBtn.setEnabled( value );
        checkAllBtn.setEnabled( value );
        destroyClusterBtn.setEnabled( value );
        addNodeBtn.setEnabled( value );
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
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executor, commandRunner,
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


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( nodesTable, config.getSlaveNodes(), config.getMasterNode() );
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
        if ( clustersInfo != null && clustersInfo.size() > 0 )
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


    /**
     * @param agent agent
     *
     * @return Yes if give agent is among seeds, otherwise returns No
     */
    public String checkIfMaster( Agent agent )
    {
        if ( config.getMasterNode().equals( agent ) )
        {
            return "Master";
        }
        return "Slave";
    }


    protected Button getCheckButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( CHECK_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
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
                    Button checkBtn = getCheckButton( availableOperationsLayout );
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


    private void populateTable( final Table table, Set<Agent> agents, final Agent master )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.addStyleName( "default" );

            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.addStyleName( "default" );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );

            availableOperations.addComponent( checkBtn );
            availableOperations.addComponent( startBtn );
            availableOperations.addComponent( stopBtn );
            availableOperations.addComponent( destroyBtn );

            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), checkIfMaster( agent ), resultHolder,
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
                    destroyBtn.setEnabled( false );

                    executor.execute( new CheckTaskSlave( spark, tracker, config.getClusterName(), agent.getHostname(),
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
                    destroyBtn.setEnabled( false );
                    checkBtn.setEnabled( false );

                    executor.execute(
                            new StartTask( spark, tracker, config.getClusterName(), agent.getHostname(), false,
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
                    destroyBtn.setEnabled( false );
                    checkBtn.setEnabled( false );
                    executor.execute( new StopTask( spark, tracker, config.getClusterName(), agent.getHostname(), false,
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
                            UUID trackID = spark.destroySlaveNode( config.getClusterName(), agent.getHostname() );
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

        //add master here
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

        final HorizontalLayout availableOperations = new HorizontalLayout();
        availableOperations.addStyleName( "default" );
        availableOperations.setSpacing( true );

        availableOperations.addComponent( checkBtn );
        availableOperations.addComponent( startBtn );
        availableOperations.addComponent( stopBtn );


        table.addItem( new Object[] {
                master.getHostname(), master.getListIP().get( 0 ), checkIfMaster( master ), resultHolder,
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

                executor.execute( new CheckTaskMaster( spark, tracker, config.getClusterName(), master.getHostname(),
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
                checkBtn.setEnabled( false );

                executor.execute( new StartTask( spark, tracker, config.getClusterName(), master.getHostname(), true,
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
                checkBtn.setEnabled( false );

                executor.execute( new StopTask( spark, tracker, config.getClusterName(), master.getHostname(), true,
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


    public Component getContent()
    {
        return contentRoot;
    }
}
