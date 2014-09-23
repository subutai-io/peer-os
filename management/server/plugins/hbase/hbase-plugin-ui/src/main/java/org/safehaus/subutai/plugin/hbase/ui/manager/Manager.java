/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.manager;


import com.google.common.base.Preconditions;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import javax.naming.NamingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Enum.valueOf;
import static org.safehaus.subutai.plugin.hbase.api.HBaseType.HRegionServer;


public class Manager
{
    protected final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected final static String REFRESH_CLUSTER_CAPTION = "Refresh Clusters";
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
    protected final static String MASTER_TABLE_CAPTION = "HMaster";
    protected final static String REGION_SERVERS_TABLE_CAPTION = "Region Servers";
    protected final static String QUORUM_PEERS_TABLE_CAPTION = "Quorum Peers";
    protected final static String BACKUP_MASTERS_TABLE_CAPTION = "Backup Masters";
    protected final static String BUTTON_STYLE_NAME = "default";


    protected final Button refreshClustersBtn, startAllNodesBtn, stopAllNodesBtn, checkAllBtn, destroyClusterBtn;

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table masterTable;
    private final Table regionTable;
    private final Table quorumTable;
    private final Table backUpMasterTable;
    private final ExecutorService executor;
    private HBaseClusterConfig config;

    private final HBase hbase;
    private final Tracker tracker;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final String MESSAGE = "No cluster is installed !";
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final Pattern HMASTER_PATTERN = Pattern.compile( ".*(HMaster.+?g).*" );
    private final Pattern REGION_PATTERN = Pattern.compile( ".*(HRegionServer.+?g).*" );
    private final Pattern QUORUM_PATTERN = Pattern.compile( ".*(HQuorumPeer.+?g).*" );



    public Manager( final ExecutorService executor, final ServiceLocator serviceLocator ) throws NamingException
    {
        Preconditions.checkNotNull( executor, "Executor is null" );
        Preconditions.checkNotNull( serviceLocator, "Service Locator is null" );

        this.hbase = serviceLocator.getService( HBase.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.executor = executor;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        contentRoot.addComponent( content );
        contentRoot.setComponentAlignment( content, Alignment.TOP_CENTER );
        contentRoot.setMargin( true );

        //tables go here
        masterTable = createTableTemplate( MASTER_TABLE_CAPTION );
        regionTable = createTableTemplate( REGION_SERVERS_TABLE_CAPTION );
        quorumTable = createTableTemplate( QUORUM_PEERS_TABLE_CAPTION );
        backUpMasterTable = createTableTemplate( BACKUP_MASTERS_TABLE_CAPTION );


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
                Object value = event.getProperty().getValue();
                config = value != null ? ( HBaseClusterConfig ) value : null;
                refreshUI();
            }
        } );
        controlsContent.addComponent( clusterCombo );


        /** Refresh Cluster button */
        refreshClustersBtn = new Button( REFRESH_CLUSTER_CAPTION );
        refreshClustersBtn.addStyleName( BUTTON_STYLE_NAME );
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
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.addStyleName(BUTTON_STYLE_NAME );
        checkAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( config == null )
                {
                    show( MESSAGE );
                }
                else
                {
                    checkAllNodes( masterTable );
                    checkAllNodes( regionTable );
                    checkAllNodes( quorumTable );
                    checkAllNodes( backUpMasterTable );
                }
            }
        } );
        controlsContent.addComponent( checkAllBtn );


        /** Start All button */
        startAllNodesBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllNodesBtn.addStyleName( BUTTON_STYLE_NAME );
        startAllNodesBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if( config != null ) {
                    UUID trackID = hbase.startCluster( config.getClusterName() );
                    ProgressWindow window = new ProgressWindow( executor, tracker, trackID,
                            HBaseClusterConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener() {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent ) {
                            refreshClustersInfo();
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                } else {
                    show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( startAllNodesBtn );


        /** Stop All button */
        stopAllNodesBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllNodesBtn.addStyleName( BUTTON_STYLE_NAME );
        stopAllNodesBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if( config != null ) {
                    UUID trackID = hbase.stopCluster( config.getClusterName() );
                    ProgressWindow window = new ProgressWindow( executor, tracker, trackID,
                            HBaseClusterConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener() {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent ) {
                            refreshClustersInfo();
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                } else {
                    show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( stopAllNodesBtn );


        /** Destroy All button */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.addStyleName( BUTTON_STYLE_NAME );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
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
                            UUID trackID = hbase.uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( executor, tracker, trackID,
                                            HBaseClusterConfig.PRODUCT_KEY );
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

        PROGRESS_ICON.setVisible( false );
        controlsContent.addComponent( PROGRESS_ICON );

        content.addComponent( controlsContent );
        content.addComponent( masterTable );
        content.addComponent( regionTable );
        content.addComponent( quorumTable );
        content.addComponent( backUpMasterTable );
    }


    public void checkAllNodes( Table table)
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

    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( regionTable, config.getRegionServers(), HBaseType.HRegionServer );
            populateTable( quorumTable, config.getQuorumPeers(), HBaseType.HQuorumPeer );
            populateTable( backUpMasterTable, config.getBackupMasters(), HBaseType.BackupMaster );

            Set<Agent> masterSet = new HashSet<>();
            masterSet.add( config.getHbaseMaster() );
            populateMasterTable( masterTable, masterSet );
        }
        else
        {
            regionTable.removeAllItems();
            quorumTable.removeAllItems();
            backUpMasterTable.removeAllItems();
            masterTable.removeAllItems();
        }
    }


    private void populateMasterTable( final Table table, Set<Agent> agents )
    {
        table.removeAllItems();
        for ( final Agent agent : agents )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.addStyleName( BUTTON_STYLE_NAME );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.addStyleName( BUTTON_STYLE_NAME );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.addStyleName( BUTTON_STYLE_NAME );


            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            PROGRESS_ICON.setVisible( false );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( BUTTON_STYLE_NAME );
            availableOperations.setSpacing( true );

            availableOperations.addComponent( checkBtn );
//            availableOperations.addComponent( startBtn );
//            availableOperations.addComponent( stopBtn );


            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), "HMaster", resultHolder, availableOperations
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    PROGRESS_ICON.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    checkBtn.setEnabled( false );
                    executor.execute(
                            new CheckTask( hbase, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        public void onComplete( String result )
                                        {
                                            synchronized ( PROGRESS_ICON )
                                            {

                                                resultHolder.setValue( parseStatus( result, HMASTER_PATTERN ));
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
                                                PROGRESS_ICON.setVisible( false );
                                                checkBtn.setEnabled( true );
                                            }
                                        }
                                    } ) );
                }
            } );
        }
    }


    private void populateTable( final Table table, Set<Agent> agents, final HBaseType role )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.addStyleName( BUTTON_STYLE_NAME );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.addStyleName( BUTTON_STYLE_NAME );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.addStyleName( BUTTON_STYLE_NAME );

            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            PROGRESS_ICON.setVisible( false );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( BUTTON_STYLE_NAME );
            availableOperations.setSpacing( true );

            availableOperations.addComponent( checkBtn );
//            availableOperations.addComponent( startBtn );
//            availableOperations.addComponent( stopBtn );


            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), role.name(), resultHolder, availableOperations
            }, null );


            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    PROGRESS_ICON.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    checkBtn.setEnabled( false );
                    executor.execute(
                            new CheckTask( hbase, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        public void onComplete( String result )
                                        {
                                            synchronized ( PROGRESS_ICON )
                                            {
                                                String status = "UNKNOWN";
                                                switch( role ) {
                                                    case HRegionServer:
                                                        status = parseStatus( result, REGION_PATTERN );
                                                        break;
                                                    case HQuorumPeer:
                                                        status = parseStatus( result, QUORUM_PATTERN );
                                                        break;
                                                    case BackupMaster:
                                                        status = parseStatus( result, HMASTER_PATTERN );
                                                        break;
                                                }
                                                resultHolder.setValue( status );
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
                                                PROGRESS_ICON.setVisible( false );
                                                checkBtn.setEnabled( true );
                                            }
                                        }
                                    } ) );
                }
            } );

        }
    }


    public void refreshClustersInfo()
    {
        List<HBaseClusterConfig> clusters = hbase.getClusters();
        HBaseClusterConfig clusterInfo = ( HBaseClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clusters != null && clusters.size() > 0 )
        {
            for ( HBaseClusterConfig info : clusters )
            {
                clusterCombo.addItem( info );
                clusterCombo.setItemCaption( info, info.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( HBaseClusterConfig c : clusters )
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
                clusterCombo.setValue( clusters.iterator().next() );
            }
        }
    }

    private String parseStatus( String result, Pattern pattern)
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher masterMatcher = pattern.matcher( result );
        if ( masterMatcher.find() )
        {
            parsedResult.append( masterMatcher.group( 1 ) ).append( " " );
        }
        return parsedResult.toString();
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

        table.setColumnExpandRatio( HOST_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( IP_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( NODE_ROLE_COLUMN_CAPTION, 0.15f );
        table.setColumnExpandRatio( STATUS_COLUMN_CAPTION, 0.25f );
        table.setColumnExpandRatio( AVAILABLE_OPERATIONS_COLUMN_CAPTION, 0.40f );

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
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executor,
                                        commandRunner, agentManager );
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


    private void show( String notification )
    {
        Notification.show( notification );
    }


    public static void checkNodesStatus( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.click();
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
