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
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.common.ui.AddNodeWindow;
import org.safehaus.subutai.plugin.common.ui.BaseManager;
import org.safehaus.subutai.plugin.common.ui.OperationTask;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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

    //public final static String SERVER_TABLE_CAPTION = "Server";
    //public final static String CLIENT_TABLE_CAPTION = "Clients";

    public final static String NODES_TABLE_CAPTION = "Nodes";

    private final ComboBox clusterCombo;
    //private final Table serverTable;
    private final Table nodesTable;
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
        //serverTable = createTableTemplate( SERVER_TABLE_CAPTION );
        //clientsTable = createTableTemplate( CLIENT_TABLE_CAPTION );

        nodesTable = createTableTemplate(NODES_TABLE_CAPTION);
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
                config = ( OozieClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
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
        addNodeButton.addStyleName( "default" );
        addNodeButton.addClickListener( addNodeButtonListener() );

        controlsContent.addComponent( addNodeButton );
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.addComponent( getProgressBar() );

        contentRoot.addComponent( controlsContent, 0, 0 );
        //contentRoot.addComponent( serverTable, 0, 1, 0, 5 );
        //contentRoot.addComponent( clientsTable, 0, 6, 0, 10 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 5 );

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
                            HashSet<Agent> nodes = new HashSet<>( info.getAllNodes() );
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
                                UUID trackId = oozieManager.addNode( config.getClusterName(), null );
                                ProgressWindow w = new ProgressWindow( executorService, tracker, trackId,
                                        config.getProductKey() );
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
            //populateServerTable( serverTable, config.getServer() );
            //populateClientsTable( clientsTable, config.getClients() );
            //clickAllCheckButtons( serverTable );

            populateClientsTable( nodesTable, config.getClients());
            checkServer();
        }
        else
        {
            /*serverTable.removeAllItems();
            clientsTable.removeAllItems();*/
            nodesTable.removeAllItems();
        }
    }
    public void checkServer()
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


    private void populateServerTable( final Table table, final Agent agent )
    {
        List<Agent> agentList = new ArrayList<>();
        agentList.add( agent );
        agentList.add(config.getServer());
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


    private void populateClientsTable( final Table table, Set<Agent> clientNodes )
    {

        table.removeAllItems();
        List<Agent> agentList = new ArrayList<>();
        agentList.addAll( clientNodes );

        agentList.add(config.getServer());

        populateTable( table, agentList );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    public Table createTableTemplate( String caption ) {
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


    public Component getContent()
    {
        return contentRoot;
    }


    @Override
    public void addRowComponents( Table table, final Agent agent )
    {
        Preconditions.checkNotNull( table, "Cannot add components to not existing table" );
        Preconditions.checkNotNull( agent, "Cannot add null agent to the table" );
        /*if ( table.getCaption().equals( SERVER_TABLE_CAPTION ) ) {
            addServerRow( table, agent );
        }
        else {*/
        if ( config.getServer().equals( agent ) ) {
            addServerRow(table,agent);
        }
        else {
            addClientRow( table, agent );
        }
        //}
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
        final Button startStopButton = new Button( START_STOP_BUTTON_DEFAULT_CAPTION );

        checkButton.addStyleName( "default" );
        startStopButton.addStyleName( "default" );
        // Buttons to be added to availableOperations

        // Labels to be added to statusGroup
        final Label statusLabel = new Label( "" );

        statusLabel.addStyleName( "default" );
        // Labels to be added to statusGroup

        availableOperations.addComponent( checkButton );
        availableOperations.addComponent( startStopButton );
        statusGroup.addComponent( statusLabel );

        table.addItem( new Object[] {
                agent.getHostname(), agent.getListIP().toString(), statusGroup, availableOperations
        }, null );

        Item row = getAgentRow( table, agent );

        checkButton.addClickListener( checkButtonListener( row ) );
        startStopButton.addClickListener( startStopButtonListener( row ) );
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

        destroyButton.addStyleName( "default" );
        // Buttons to be added to availableOperations

        availableOperations.addComponent( destroyButton );

        table.addItem( new Object[] {
                agent.getHostname(), agent.getListIP().toString(),statusLayout, availableOperations
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

                OperationType operationType;
                if ( !isRunning ) {
                    operationType = OperationType.Start;
                }
                else {
                    operationType = OperationType.Stop;
                }
                executorService
                        .execute( new OperationTask( oozieManager, tracker, operationType,
                                NodeType.SERVER, config, startStopCheckCompleteEvent( row ), null, agent ) );
            }
        } ;
    }


    private CompleteEvent startStopCheckCompleteEvent( Item row )
    {
        HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
        final Button checkButton = getCheckButton( availableOperationsLayout );
        final Button startStopButton = getStartStopButton( availableOperationsLayout );
        HorizontalLayout statusLayout = getStatusLayout( row );
        final Label statusLabel = getStatusLabel( statusLayout );

        return new CompleteEvent()
        {

            public void onComplete( NodeState state )
            {
                if ( state == NodeState.RUNNING )
                {
                    statusLabel.setValue( "Server is running" );
                    startStopButton.setCaption( STOP_BUTTON_CAPTION );
                    startStopButton.setEnabled( true );
                }
                else if ( state == NodeState.STOPPED )
                {
                    statusLabel.setValue( "Server is stopped" );
                    startStopButton.setCaption( START_BUTTON_CAPTION );
                    startStopButton.setEnabled( true );
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


    private Button.ClickListener destroyButtonListener( final Item row )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                Button destroyButton = clickEvent.getButton();
                enableProgressBar();
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
                        disableProgressBar();
                    }
                } );
                contentRoot.getUI().addWindow( window.getWindow() );
            }
        } ;
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
                final Button startStopButton = getStartStopButton( availableOperationsLayout );
                checkButton.setEnabled( false );
                enableProgressBar();
                startStopButton.setEnabled( false );
                checkButton.setEnabled( false );
                Agent agent = getAgentByRow( row );

                OperationType operationType = OperationType.Status;
                executorService
                        .execute( new OperationTask( oozieManager, tracker, operationType,
                                NodeType.SERVER, config, startStopCheckCompleteEvent( row ), null, agent ) );

            }
        };
    }


    protected Agent getAgentByRow( final Item row ) {
        if ( row == null ) {
            return null;
        }

        Set<Agent> clusterNodeList = config.getAllOozieAgents();
        String lxcHostname= row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();

        for ( Agent agent : clusterNodeList ) {
            if ( agent.getHostname().equals( lxcHostname ) ) {
                return agent;
            }
        }
        return null;
    }


    private Label getStatusLabel( final HorizontalLayout statusGroupLayout ) {
        if ( statusGroupLayout == null ) {
            return null;
        }
        return (Label) statusGroupLayout.getComponent( 0 );
    }
}
