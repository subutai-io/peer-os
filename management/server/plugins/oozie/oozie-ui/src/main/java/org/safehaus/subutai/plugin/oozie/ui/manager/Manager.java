package org.safehaus.subutai.plugin.oozie.ui.manager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.ui.AddNodeWindow;
import org.safehaus.subutai.plugin.common.ui.BaseManager;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.google.common.base.Preconditions;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager extends BaseManager
{

    public final static String SERVER_TABLE_CAPTION = "Server";
    public final static String CLIENT_TABLE_CAPTION = "Clients";

    private final ComboBox clusterCombo;
    private final Table serverTable;
    private final Table clientsTable;
    private final Oozie oozieManager;
    private final Hadoop hadoopManager;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final CommandRunner commandRunner;
    private final AgentManager agentManager;
    private OozieClusterConfig config;


    public Manager( final ExecutorService executorService, final ServiceLocator serviceLocator ) throws NamingException
    {
        super();
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoopManager = serviceLocator.getService( Hadoop.class );
        this.oozieManager = serviceLocator.getService( Oozie.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        serverTable = createTableTemplate( SERVER_TABLE_CAPTION );
        serverTable.setId( "OozieMngServerTable" );
        clientsTable = createTableTemplate( CLIENT_TABLE_CAPTION );
        clientsTable.setId( "OozieMngClientsTable" );

        /*
        nodesTable = createTableTemplate(NODES_TABLE_CAPTION);
        nodesTable.setId("OozieMngNodesTable");
*/
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "OozieMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( OozieClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.setId( "OozieMngRefresh" );
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

        /*Button checkAllBtn = new Button( "Check all" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                clickAllCheckButtons( serverTable );
            }
        } );*/

        Button destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "OozieMngDestroy" );
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
                            UUID trackID = oozieManager.uninstallCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    OozieClusterConfig.PRODUCT_KEY );
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


        Button addNodeButton = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeButton.setId( "OozieMngAddNode" );
        addNodeButton.addStyleName( "default" );
        addNodeButton.addClickListener( addNodeButtonListener() );

        controlsContent.addComponent( addNodeButton );
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.addComponent( getProgressBar() );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( serverTable, 0, 1, 0, 5 );
        contentRoot.addComponent( clientsTable, 0, 6, 0, 10 );
        //contentRoot.addComponent( nodesTable, 0, 1, 0, 5 );

    }


    public Button.ClickListener addNodeButtonListener()
    {
        return new Button.ClickListener()
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
                    if ( config.getSetupType() == SetupType.OVER_HADOOP )
                    {
                        String hadoopClusterName = config.getHadoopClusterName();
                        if ( hadoopClusterName == null || hadoopClusterName.isEmpty() )
                        {
                            show( "Undefined Hadoop cluster name" );
                            return;
                        }
                        HadoopClusterConfig info = hadoopManager.getCluster( hadoopClusterName );
                        if ( info != null )
                        {
                            HashSet<UUID> nodes = new HashSet<>( info.getAllNodes() );
                            nodes.removeAll( config.getAllOozieAgents() );
                            if ( !nodes.isEmpty() )
                            {
                                AddNodeWindow addNodeWindow =
                                        new AddNodeWindow( oozieManager, executorService, tracker, config, nodes );
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
                                show( "All nodes in corresponding Hadoop cluster have Oozie installed" );
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
                                UUID trackId = oozieManager.addNode( config.getClusterName(), null );
                                ProgressWindow w =
                                        new ProgressWindow( executorService, tracker, trackId, config.getProductKey() );
                                contentRoot.getUI().addWindow( w.getWindow() );
                            }
                        } );
                        contentRoot.getUI().addWindow( d.getAlert() );
                    }
                }
            }
        };
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateServerTable( serverTable, config.getServer() );
            populateClientsTable( clientsTable, config.getClients() );
            clickAllCheckButtons( serverTable );
        }
        else
        {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }


    private void populateServerTable( final Table table, final Agent agent )
    {
        List<Agent> agentList = new ArrayList<>();
        agentList.add( agent );
        //agentList.add(config.getServer());
        populateTable( table, agentList );
    }


    private void populateClientsTable( final Table table, Set<Agent> clientNodes )
    {

        table.removeAllItems();
        List<UUID> agentList = new ArrayList<>();
        agentList.addAll( clientNodes );
        populateTable( table, agentList );
    }


    public void refreshClustersInfo()
    {
        List<OozieClusterConfig> info = oozieManager.getClusters();
        OozieClusterConfig clusterInfo = ( OozieClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( info != null && !info.isEmpty() )
        {
            for ( OozieClusterConfig oozieConfig : info )
            {
                clusterCombo.addItem( oozieConfig );
                clusterCombo.setItemCaption( oozieConfig, oozieConfig.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( OozieClusterConfig oozieInfo : info )
                {
                    if ( oozieInfo.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( oozieInfo );
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


    @Override
    public void addRowComponents( Table table, final Agent agent )
    {
        Preconditions.checkNotNull( table, "Cannot add components to not existing table" );
        Preconditions.checkNotNull( agent, "Cannot add null agent to the table" );
        if ( table.getCaption().equals( SERVER_TABLE_CAPTION ) )
        {
            addServerRow( table, agent );
        }
        else
        {
            addClientRow( table, agent );
        }
    }


    public Table createTableTemplate( String caption )
    {

        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( IP_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( STATUS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setColumnExpandRatio( HOST_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( IP_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( STATUS_COLUMN_CAPTION, 0.40f );
        table.setColumnExpandRatio( AVAILABLE_OPERATIONS_COLUMN_CAPTION, 0.40f );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

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
                        /*TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executorService, commandRunner,
                                        agentManager );
                        contentRoot.getUI().addWindow( terminal.getWindow() );*/
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


    private void addServerRow( final Table table, final Agent agent )
    {
        // Layouts to be added to table
        final HorizontalLayout availableOperations = new HorizontalLayout();
        final HorizontalLayout statusGroup = new HorizontalLayout();
        availableOperations.addStyleName( "default" );
        availableOperations.setSpacing( true );
        statusGroup.addStyleName( "default" );
        statusGroup.setSpacing( true );
        // Layouts to be added to table

        // Buttons to be added to availableOperations
        final Button checkButton = new Button( CHECK_BUTTON_CAPTION );
        checkButton.setId( agent.getListIP().get( 0 ) + "-oozieCheck" );
        final Button startButton = new Button( START_BUTTON_CAPTION );
        startButton.setId( agent.getListIP().get( 0 ) + "-oozieStart" );
        final Button stopButton = new Button( STOP_BUTTON_CAPTION );
        stopButton.setId( agent.getListIP().get( 0 ) + "-oozieStop" );

        checkButton.addStyleName( "default" );
        startButton.addStyleName( "default" );
        stopButton.addStyleName( "default" );
        // Buttons to be added to availableOperations

        // Labels to be added to statusGroup
        final Label statusLabel = new Label( "" );

        statusLabel.addStyleName( "default" );
        // Labels to be added to statusGroup

        availableOperations.addComponent( checkButton );
        availableOperations.addComponent( startButton );
        availableOperations.addComponent( stopButton );
        statusGroup.addComponent( statusLabel );

        table.addItem( new Object[] {
                agent.getHostname(), agent.getListIP().get( 0 ).toString(), statusGroup, availableOperations
        }, null );

        Item row = getAgentRow( table, agent );

        checkButton.addClickListener( checkButtonListener( row ) );
        startButton.addClickListener( startButtonListener( row ) );
        stopButton.addClickListener( stopButtonListener( row ) );
    }


    private void addClientRow( final Table table, final Agent agent )
    {
        // Layouts to be added to table
        final HorizontalLayout availableOperations = new HorizontalLayout();
        final HorizontalLayout statusLayout = new HorizontalLayout();
        availableOperations.addStyleName( "default" );
        availableOperations.setSpacing( true );
        // Layouts to be added to table

        // Buttons to be added to availableOperations
        final Button destroyButton = new Button( DESTROY_BUTTON_CAPTION );
        destroyButton.setId( agent.getListIP().get( 0 ) + "-oozieDestroy" );

        destroyButton.addStyleName( "default" );
        // Buttons to be added to availableOperations

        availableOperations.addComponent( destroyButton );

        table.addItem( new Object[] {
                agent.getHostname(), agent.getListIP().get( 0 ).toString(), statusLayout, availableOperations
        }, null );

        Item row = getAgentRow( table, agent );

        destroyButton.addClickListener( destroyButtonListener( row ) );
    }


    private Button.ClickListener startStopButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                final Button startStopButton = clickEvent.getButton();
                HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
                final Button checkButton = getCheckButton( availableOperationsLayout );

                startStopButton.setEnabled( false );
                boolean isRunning = startStopButton.getCaption().contains( STOP_BUTTON_CAPTION );
                enableProgressBar();
                startStopButton.setEnabled( false );
                checkButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                NodeOperationType operationType;
                if ( !isRunning )
                {
                    operationType = NodeOperationType.Start;
                }
                else
                {
                    operationType = NodeOperationType.Stop;
                }
                executorService.execute(
                        new OperationTask( oozieManager, tracker, operationType, NodeType.SERVER, config,
                                startStopCheckCompleteEvent( row ), null, agent ) );
            }
        };
    }


    private CompleteEvent startStopCheckCompleteEvent( Item row )
    {
        HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
        final Button checkButton = getCheckButton( availableOperationsLayout );
        final Button startButton = getStartButton( availableOperationsLayout );
        final Button stopButton = getStopButton( availableOperationsLayout );
        HorizontalLayout statusLayout = getStatusLayout( row );
        final Label statusLabel = getStatusLabel( statusLayout );

        return new CompleteEvent()
        {

            public void onComplete( NodeState state )
            {
                if ( state == NodeState.RUNNING )
                {
                    statusLabel.setValue( "Server is running" );
                    //stopButton.setCaption( STOP_BUTTON_CAPTION );
                    stopButton.setEnabled( true );
                }
                else if ( state == NodeState.STOPPED )
                {
                    statusLabel.setValue( "Server is stopped" );
                    //startButton.setCaption( START_BUTTON_CAPTION );
                    startButton.setEnabled( true );
                }
                else
                {
                    statusLabel.setValue( "Server is not connected" );
                }

                checkButton.setEnabled( true );
                disableProgressBar();
            }
        };
    }


    private Label getStatusLabel( final HorizontalLayout statusGroupLayout )
    {
        if ( statusGroupLayout == null )
        {
            return null;
        }
        return ( Label ) statusGroupLayout.getComponent( 0 );
    }


    protected Agent getAgentByRow( final Item row )
    {
        if ( row == null )
        {
            return null;
        }

        Set<Agent> clusterNodeList = config.getAllOozieAgents();
        String lxcHostname = row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();

        for ( Agent agent : clusterNodeList )
        {
            if ( agent.getHostname().equals( lxcHostname ) )
            {
                return agent;
            }
        }
        return null;
    }


    private Button.ClickListener destroyButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                Button destroyButton = clickEvent.getButton();
                //enableProgressBar();
                destroyButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                UUID trackID = oozieManager.destroyNode( config.getClusterName(), agent.getHostname() );

                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, OozieClusterConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                        refreshUI();
                        //disableProgressBar();
                    }
                } );
                contentRoot.getUI().addWindow( window.getWindow() );
            }
        };
    }


    private Button.ClickListener startButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                final Button startButton = clickEvent.getButton();
                final Button stopButton = clickEvent.getButton();
                HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
                final Button checkButton = getCheckButton( availableOperationsLayout );

                startButton.setEnabled( false );
                stopButton.setEnabled( false );
                boolean isRunning = startButton.isEnabled();
                enableProgressBar();
                startButton.setEnabled( false );
                stopButton.setEnabled( false );
                checkButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                NodeOperationType operationType;
                if ( !isRunning )
                {
                    operationType = NodeOperationType.Start;

                    executorService.execute(
                            new OperationTask( oozieManager, tracker, operationType, NodeType.SERVER, config,
                                    startStopCheckCompleteEvent( row ), null, agent ) );
                }
            }
        };
    }


    private Button.ClickListener stopButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                final Button startButton = clickEvent.getButton();
                final Button stopButton = clickEvent.getButton();
                HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
                final Button checkButton = getCheckButton( availableOperationsLayout );

                startButton.setEnabled( false );
                stopButton.setEnabled( false );
                boolean isRunning = stopButton.isEnabled();
                enableProgressBar();
                startButton.setEnabled( false );
                stopButton.setEnabled( false );
                checkButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                NodeOperationType operationType;
                if ( !isRunning )
                {
                    operationType = NodeOperationType.Stop;

                    executorService.execute(
                            new OperationTask( oozieManager, tracker, operationType, NodeType.SERVER, config,
                                    startStopCheckCompleteEvent( row ), null, agent ) );
                }
            }
        };
    }


    private Button.ClickListener checkButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {

                final Button checkButton = clickEvent.getButton();
                HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
                final Button startButton = getStartButton( availableOperationsLayout );
                final Button stopButton = getStopButton( availableOperationsLayout );
                checkButton.setEnabled( false );
                enableProgressBar();
                startButton.setEnabled( false );
                stopButton.setEnabled( false );
                checkButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                NodeOperationType operationType = NodeOperationType.Status;
                executorService.execute(
                        new OperationTask( oozieManager, tracker, operationType, NodeType.SERVER, config,
                                startStopCheckCompleteEvent( row ), null, agent ) );
            }
        };
    }
}
