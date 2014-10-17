package org.safehaus.subutai.plugin.hive.ui.manager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
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
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class Manager
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String SERVER_TABLE_CAPTION = "Server Nodes";
    protected static final String CLIENT_TABLE_CAPTION = "Client Nodes";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String BUTTON_STYLE_NAME = "default";
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private static final String MESSAGE = "No cluster is installed !";
    final Button refreshClustersBtn, destroyClusterBtn, addNodeBtn;

    private final ComboBox clusterCombo;
    private final Table serverTable, clientsTable;
    private final Hive hive;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final CommandRunner commandRunner;
    private final AgentManager agentManager;
    private GridLayout contentRoot;
    private HiveConfig config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executorService = executorService;
        this.hive = serviceLocator.getService( Hive.class );
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
        serverTable = createTableTemplate(SERVER_TABLE_CAPTION);
        serverTable.setId("HiveTable");
        clientsTable = createTableTemplate(CLIENT_TABLE_CAPTION);
        clientsTable.setId("HiveClientsTable");
        /*
        nodesTable = createTableTemplate( SERVER_TABLE_CAPTION );
        nodesTable.setId("HiveTable");
*/

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId("HiveClusterCb");
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( HiveConfig ) event.getProperty().getValue();
                refreshUI();
                checkServer();
            }
        } );

        /** Refresh Cluster Button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId("hiveRefreshClusterBtn");
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );


        /** Destroy Cluster Button */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId("HiveDestroyClusterBtn");
        addClickListenerToDestroyClusterButton();


        /** Add Node Button */
        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId("HiveAddNodeBtn");
        addClickListenerToAddNodeButton();


        addStyleNameToButtons( refreshClustersBtn, destroyClusterBtn, addNodeBtn );
        addGivenComponents( controlsContent, clusterCombo, refreshClustersBtn, destroyClusterBtn, addNodeBtn );
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );
        controlsContent.setComponentAlignment( addNodeBtn, Alignment.MIDDLE_CENTER );

        VerticalLayout tablesLayout = new VerticalLayout();
        tablesLayout.setSizeFull();
        tablesLayout.setSpacing( true );

        addGivenComponents( tablesLayout, serverTable );
        addGivenComponents( tablesLayout, clientsTable );


        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId("indicator");
        controlsContent.addComponent( PROGRESS_ICON );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( tablesLayout, 0, 1, 0, 9 );
    }


    private void addClickListenerToAddNodeButton()
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
                Set<Agent> set = new HashSet<>( config.getHadoopNodes() );
                set.remove( config.getServer() );
                set.removeAll( config.getClients() );
                if ( set.isEmpty() )
                {
                    show( "All nodes in Hadoop cluster have Hive installed" );
                    return;
                }
                AddNodeWindow w = new AddNodeWindow( hive, executorService, tracker, config, set );
                contentRoot.getUI().addWindow( w );
                w.addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                        refreshUI();
                        checkServer();
                    }
                } );
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
                if ( config == null )
                {
                    show( "Select cluster" );
                    return;
                }
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Cluster '%s' will be destroyed. Continue?", config.getClusterName() ), "Yes",
                        "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        destroyClusterHandler();
                    }
                } );

                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void destroyClusterHandler()
    {
        UUID trackID = hive.uninstallCluster( config.getClusterName() );
        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID, HiveConfig.PRODUCT_KEY );
        window.getWindow().addCloseListener( new Window.CloseListener()
        {
            @Override
            public void windowClose( Window.CloseEvent closeEvent )
            {
                refreshUI();
                refreshClustersInfo();
            }
        } );
        contentRoot.getUI().addWindow( window.getWindow() );
    }


    public void checkServer()
    {
        if ( serverTable != null )
        {
            for ( Object o : serverTable.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = serverTable.getItem( rowId );
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
        table.addContainerProperty( NODE_ROLE_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        addClickListenerToTable( table );
        return table;
    }


    private void addClickListenerToTable( final Table table )
    {
        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                String lxcHostname = ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
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
        } );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    public void refreshUI()
    {
        if ( config != null )
        {
            Set<Agent> cli_agents = new HashSet<>();
            cli_agents.add(config.getServer());
            populateTable( serverTable, cli_agents );
            populateTable( clientsTable, config.getClients() );
        }
        else
        {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, Set<Agent> agents )
    {
        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.setId(agent.getListIP().get(0)+"-hiveCheck");
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.setId(agent.getListIP().get(0)+"-hiveStart");
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.setId(agent.getListIP().get(0)+"-hiveStop");
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.setId(agent.getListIP().get(0)+"-hiveDestroy");

            addStyleNameToButtons( checkBtn, startBtn, stopBtn, destroyBtn );
            disableButtons( startBtn, stopBtn );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );

            if ( isServer( agent ) )
            {
                addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn );
            }
            else
            {
                addGivenComponents( availableOperations, destroyBtn );
                destroyBtn.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        ConfirmationDialog alert = new ConfirmationDialog(
                                String.format( "Do you want to destroy node  %s?", agent.getHostname() ), "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                UUID trackID = hive.destroyNode( config.getClusterName(), agent.getHostname() );
                                ProgressWindow window =
                                        new ProgressWindow( executorService, tracker, trackID, HiveConfig.PRODUCT_KEY );
                                window.getWindow().addCloseListener( new Window.CloseListener()
                                {
                                    @Override
                                    public void windowClose( Window.CloseEvent closeEvent )
                                    {
                                        refreshClustersInfo();
                                        refreshUI();
                                        checkServer();
                                    }
                                } );
                                contentRoot.getUI().addWindow( window.getWindow() );
                            }
                        } );

                        contentRoot.getUI().addWindow( alert.getAlert() );
                    }
                } );
            }

            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ),checkNodeRole(agent), availableOperations
            }, null );

            addClickListenerToCheckButton( agent, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToStartButton( agent, startBtn, stopBtn, checkBtn, destroyBtn );
            addClickListenerToStopButton( agent, startBtn, stopBtn, checkBtn, destroyBtn );
        }
    }


    public String checkNodeRole( Agent agent )
    {

        if ( config.getServer().equals( agent ) )
        {
            return "Server";
        }
        else
        {
            return "Client";
        }
    }


    private boolean isServer( Agent agent )
    {
        return config.getServer().equals( agent );
    }


    private void addGivenComponents( Layout layout, Component... components )
    {
        for ( Component c : components )
        {
            layout.addComponent( c );
        }
    }


    private void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    private void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    private void addClickListenerToStopButton( final Agent agent, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                disableButtons( buttons );
                final UUID trackID = hive.stopNode( config.getClusterName(), agent.getHostname() );
                PROGRESS_ICON.setVisible( true );
                executorService.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProductOperationView po = null;
                        while ( po == null || po.getState() == ProductOperationState.RUNNING )
                        {
                            po = tracker.getProductOperation( HiveConfig.PRODUCT_KEY, trackID );
                        }
                        getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                        checkServer();
                    }
                } );
            }
        } );
    }


    private void addClickListenerToStartButton( final Agent agent, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                disableButtons( buttons );
                final UUID trackID = hive.startNode( config.getClusterName(), agent.getHostname() );
                PROGRESS_ICON.setVisible( true );
                executorService.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProductOperationView po = null;
                        while ( po == null || po.getState() == ProductOperationState.RUNNING )
                        {
                            po = tracker.getProductOperation( HiveConfig.PRODUCT_KEY, trackID );
                        }
                        getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                        checkServer();
                    }
                } );
            }
        } );
    }


    private void addClickListenerToCheckButton( final Agent agent, final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                disableButtons( buttons );
                final UUID trackId = hive.statusCheck( config.getClusterName(), agent.getHostname() );
                PROGRESS_ICON.setVisible( true );
                executorService.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProductOperationView po = null;
                        while ( po == null || po.getState() == ProductOperationState.RUNNING )
                        {
                            po = tracker.getProductOperation( HiveConfig.PRODUCT_KEY, trackId );
                        }
                        PROGRESS_ICON.setVisible( false );
                        boolean running = po.getState() == ProductOperationState.SUCCEEDED;
                        getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                        getButton( START_BUTTON_CAPTION, buttons ).setEnabled( !running );
                        getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( running );

                        if ( getButton( DESTROY_BUTTON_CAPTION, buttons ) != null )
                        {
                            getButton( DESTROY_BUTTON_CAPTION, buttons ).setEnabled( true );
                        }
                    }
                } );
            }
        } );
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


    public void refreshClustersInfo()
    {
        List<HiveConfig> clusters = hive.getClusters();
        HiveConfig clusterInfo = ( HiveConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();

        if ( clusters == null || clusters.isEmpty() )
        {
            PROGRESS_ICON.setVisible( false );
            return;
        }

        for ( HiveConfig esConfig : clusters )
        {
            clusterCombo.addItem( esConfig );
            clusterCombo.setItemCaption( esConfig, esConfig.getClusterName() );
        }

        if ( clusterInfo != null )
        {
            for ( HiveConfig config : clusters )
            {
                if ( config.getClusterName().equals( clusterInfo.getClusterName() ) )
                {
                    clusterCombo.setValue( config );
                    return;
                }
            }
        }
        else
        {
            clusterCombo.setValue( clusters.iterator().next() );
        }
    }


    private void enableButtons( Button... buttons )
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
