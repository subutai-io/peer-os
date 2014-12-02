package org.safehaus.subutai.plugin.storm.ui.manager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.api.StormNodeOperationTask;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

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
    private final Table masterTable, workersTable;
    private final Storm storm;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private StormClusterConfiguration config;
    private Zookeeper zookeeper;
    private final EnvironmentManager environmentManager;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        this.executorService = executorService;
        this.storm = serviceLocator.getService( Storm.class );
        this.zookeeper = serviceLocator.getService( Zookeeper.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );


        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        masterTable = createTableTemplate( "Master node", true );
        workersTable = createTableTemplate( "Workers", false );
        masterTable.setId( "StormMngMasterNode" );
        workersTable.setId( "StormMngWorkerNodes" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "StormMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( StormClusterConfiguration ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.setId( "StormMngRefresh" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                refreshClustersInfo();
            }
        } );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.setId( "StormMngDestroy" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( config == null )
                {
                    show( "Select cluster" );
                    return;
                }

                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ), "Yes",
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

        Button addNodeBtn = new Button( "Add Node" );
        addNodeBtn.setId( "StormMngAddNode" );
        addNodeBtn.addStyleName( "default" );
        addNodeBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( config == null )
                {
                    show( "Select cluster" );
                    return;
                }

                UUID trackId = storm.addNode( config.getClusterName() );
                ProgressWindow pw =
                        new ProgressWindow( executorService, tracker, trackId, StormClusterConfiguration.PRODUCT_NAME );
                pw.getWindow().addCloseListener( new Window.CloseListener()
                {

                    @Override
                    public void windowClose( Window.CloseEvent e )
                    {
                        refreshClustersInfo();
                    }
                } );
                contentRoot.getUI().addWindow( pw.getWindow() );
            }
        } );

        controlsContent.addComponent( clusterCombo );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.addComponent( makeBatchOperationButton( "Check all", "Check" ) );
        controlsContent.addComponent( makeBatchOperationButton( "Start all", "Start" ) );
        controlsContent.addComponent( makeBatchOperationButton( "Stop all", "Stop" ) );
        controlsContent.addComponent( makeBatchOperationButton( "Restart all", "Restart" ) );
        controlsContent.addComponent( addNodeBtn );

        controlsContent.getComponent( 3 ).setId( "StormMngCheckAll" );
        controlsContent.getComponent( 4 ).setId( "StormMngStartAll" );
        controlsContent.getComponent( 5 ).setId( "StormMngStopAll" );
        controlsContent.getComponent( 6 ).setId( "StormMngRestartAll" );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( masterTable, 0, 1, 0, 5 );
        contentRoot.addComponent( workersTable, 0, 6, 0, 10 );
    }


    private Table createTableTemplate( String caption, boolean master )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Restart", Button.class, null );
        if ( !master )
        {
            table.addContainerProperty( "Destroy", Button.class, null );
        }
        table.addContainerProperty( "Status", Embedded.class, null );
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
                    String containerName =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );

                    ContainerHost containerHost = environment.getContainerHostByHostname( containerName );

                    // Check if the node is involved inside Zookeeper cluster
                    if ( containerHost == null )
                    {
                        ZookeeperClusterConfig zookeeperCluster =
                                zookeeper.getCluster( config.getZookeeperClusterName() );
                        if ( zookeeperCluster != null )
                        {
                            Environment zookeeperEnvironment =
                                    environmentManager.getEnvironmentByUUID( zookeeperCluster.getEnvironmentId() );
                            containerHost = zookeeperEnvironment.getContainerHostById( config.getNimbus() );
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


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
            Set<ContainerHost> nimbusHost = new HashSet<>();
            if ( !config.isExternalZookeeper() )
            {
                nimbusHost.add( environment.getContainerHostById( config.getNimbus() ) );
            }
            else
            {
                ZookeeperClusterConfig zookeeperCluster = zookeeper.getCluster( config.getZookeeperClusterName() );
                Environment zookeeperEnvironment =
                        environmentManager.getEnvironmentByUUID( zookeeperCluster.getEnvironmentId() );
                nimbusHost.add( zookeeperEnvironment.getContainerHostById( config.getNimbus() ) );
            }
            populateTable( masterTable, true, nimbusHost );

            Set<ContainerHost> supervisorHosts = new HashSet<>();
            for ( UUID uuid : config.getSupervisors() )
            {
                supervisorHosts.add( environment.getContainerHostById( uuid ) );
            }
            populateTable( workersTable, false, supervisorHosts );
        }
        else
        {
            masterTable.removeAllItems();
            workersTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, boolean server, Set<ContainerHost> containerHosts )
    {

        table.removeAllItems();

        for ( final ContainerHost containerHost : containerHosts )
        {
            final Button checkBtn = new Button( "Check" );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( "Start" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.addStyleName( "default" );
            final Button restartBtn = new Button( "Restart" );
            restartBtn.addStyleName( "default" );
            final Button destroyBtn = !server ? new Button( "Destroy" ) : null;
            if ( destroyBtn != null )
            {
                destroyBtn.addStyleName( "default" );
            }
            final Embedded icon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );

            startBtn.setEnabled( false );
            stopBtn.setEnabled( false );
            restartBtn.setEnabled( false );
            icon.setVisible( false );

            final List<java.io.Serializable> items = new ArrayList<>();
            items.add( containerHost.getHostname() );
            items.add( checkBtn );
            items.add( startBtn );
            items.add( stopBtn );
            items.add( restartBtn );
            if ( destroyBtn != null )
            {
                items.add( destroyBtn );
                destroyBtn.addClickListener( new Button.ClickListener()
                {

                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {

                        ConfirmationDialog alert = new ConfirmationDialog(
                                String.format( "Do you want to destroy the %s node?", containerHost.getHostname() ),
                                "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                UUID trackID =
                                        storm.destroyNode( config.getClusterName(), containerHost.getHostname() );
                                ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                        StormClusterConfiguration.PRODUCT_NAME );
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
            items.add( icon );

            table.addItem( items.toArray(), null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    icon.setVisible( true );
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    executorService.execute(
                            new StormNodeOperationTask( storm, tracker, config.getClusterName(), containerHost,
                                    NodeOperationType.STATUS, new CompleteEvent()
                            {

                                @Override
                                public void onComplete( final NodeState state )
                                {
                                    boolean running = state == NodeState.RUNNING;
                                    checkBtn.setEnabled( true );
                                    startBtn.setEnabled( !running );
                                    stopBtn.setEnabled( running );
                                    restartBtn.setEnabled( running );
                                    if ( destroyBtn != null )
                                    {
                                        destroyBtn.setEnabled( true );
                                    }
                                    icon.setVisible( false );
                                }
                            }, null ) );
                }
            } );

            startBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    icon.setVisible( true );
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    executorService.execute(
                            new StormNodeOperationTask( storm, tracker, config.getClusterName(), containerHost,
                                    NodeOperationType.START, new CompleteEvent()
                            {

                                @Override
                                public void onComplete( final NodeState state )
                                {
                                    executorService.execute(
                                            new StormNodeOperationTask( storm, tracker, config.getClusterName(),
                                                    containerHost, NodeOperationType.STATUS, new CompleteEvent()
                                            {

                                                @Override
                                                public void onComplete( final NodeState state )
                                                {
                                                    boolean running = state == NodeState.RUNNING;
                                                    checkBtn.setEnabled( true );
                                                    startBtn.setEnabled( !running );
                                                    stopBtn.setEnabled( running );
                                                    restartBtn.setEnabled( running );
                                                    if ( destroyBtn != null )
                                                    {
                                                        destroyBtn.setEnabled( true );
                                                    }
                                                    icon.setVisible( false );
                                                }
                                            }, null ) );
                                }
                            }, null ) );
                }
            } );

            stopBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    icon.setVisible( true );
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    executorService.execute(
                            new StormNodeOperationTask( storm, tracker, config.getClusterName(), containerHost,
                                    NodeOperationType.STOP, new CompleteEvent()
                            {

                                @Override
                                public void onComplete( final NodeState state )
                                {
                                    executorService.execute(
                                            new StormNodeOperationTask( storm, tracker, config.getClusterName(),
                                                    containerHost, NodeOperationType.STATUS, new CompleteEvent()
                                            {

                                                @Override
                                                public void onComplete( final NodeState state )
                                                {
                                                    boolean running = state == NodeState.RUNNING;
                                                    checkBtn.setEnabled( true );
                                                    startBtn.setEnabled( !running );
                                                    stopBtn.setEnabled( running );
                                                    restartBtn.setEnabled( running );
                                                    if ( destroyBtn != null )
                                                    {
                                                        destroyBtn.setEnabled( true );
                                                    }
                                                    icon.setVisible( false );
                                                }
                                            }, null ) );
                                }
                            }, null ) );
                }
            } );

            restartBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    icon.setVisible( true );
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    final UUID trackId = storm.restartNode( config.getClusterName(), containerHost.getHostname() );

                    executorService.execute( new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            TrackerOperationView po = null;
                            while ( po == null || po.getState() == OperationState.RUNNING )
                            {
                                po = tracker.getTrackerOperation( StormClusterConfiguration.PRODUCT_NAME, trackId );
                            }
                            boolean ok = po.getState() == OperationState.SUCCEEDED;
                            checkBtn.setEnabled( true );
                            startBtn.setEnabled( !ok );
                            stopBtn.setEnabled( ok );
                            restartBtn.setEnabled( true );
                            if ( destroyBtn != null )
                            {
                                destroyBtn.setEnabled( true );
                            }
                            icon.setVisible( false );
                        }
                    } );
                }
            } );
        }
    }


    public void refreshClustersInfo()
    {
        StormClusterConfiguration current = ( StormClusterConfiguration ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        List<StormClusterConfiguration> clustersInfo = storm.getClusters();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( StormClusterConfiguration ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                clusterCombo.setItemCaption( ci, ci.getClusterName() );
            }
            clusterCombo.setValue( current );
        }
    }


    private void destroyClusterHandler()
    {

        UUID trackID = storm.uninstallCluster( config.getClusterName() );

        ProgressWindow window =
                new ProgressWindow( executorService, tracker, trackID, StormClusterConfiguration.PRODUCT_NAME );
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


    private Button makeBatchOperationButton( String caption, final String itemProperty )
    {
        Button btn = new Button( caption );
        btn.addStyleName( "default" );
        btn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                Table[] tables = new Table[] { masterTable, workersTable };
                for ( Table t : tables )
                {
                    for ( Object itemId : t.getItemIds() )
                    {
                        Item item = t.getItem( itemId );
                        Property p = item.getItemProperty( itemProperty );
                        if ( p != null && p.getValue() instanceof Button )
                        {
                            ( ( Button ) p.getValue() ).click();
                        }
                    }
                }
            }
        } );
        return btn;
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
