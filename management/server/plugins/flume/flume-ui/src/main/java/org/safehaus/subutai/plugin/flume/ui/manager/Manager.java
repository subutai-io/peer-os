package org.safehaus.subutai.plugin.flume.ui.manager;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.NodeOperationTask;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
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
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String STYLE_NAME = "default";
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final ExecutorService executorService;
    private final Flume flume;
    private final Tracker tracker;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private Table nodesTable;
    private FlumeConfig config;
    private Hadoop hadoop;
    private final EnvironmentManager environmentManager;


    public Manager( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.flume = serviceLocator.getService( Flume.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setColumns( 1 );
        contentRoot.setRows( 10 );
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "FluNodesTbl" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        getClusterNameLabel( controlsContent );
        getClusterCombo( controlsContent );
        getRefreshClusterButton( controlsContent );
        getDestroyClusterButton( controlsContent );
        getAddNodeButton( controlsContent );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private void getAddNodeButton( HorizontalLayout controlsContent )
    {
        Button addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId( "FluAddNodeBtn" );
        addNodeBtn.addStyleName( "default" );
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    HadoopClusterConfig hadoopConfig = hadoop.getCluster( config.getHadoopClusterName() );
                    if ( hadoopConfig != null )
                    {
                        Set<UUID> nodes = new HashSet<>( hadoopConfig.getAllNodes() );
                        nodes.removeAll( config.getNodes() );
                        if ( !nodes.isEmpty() )
                        {
                            Set<ContainerHost> hosts =
                                    environmentManager.getEnvironmentByUUID( hadoopConfig.getEnvironmentId() )
                                                      .getContainerHostsByIds( nodes );
                            AddNodeWindow addNodeWindow =
                                    new AddNodeWindow( flume, tracker, executorService, config, hosts );
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
                            show( "All nodes in corresponding Hadoop cluster have Lucene installed" );
                        }
                    }
                    else
                    {
                        show( "Hadoop cluster info not found" );
                    }
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( addNodeBtn );
    }


    private void getDestroyClusterButton( HorizontalLayout controlsContent )
    {
        Button destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "FluDestroyClusterBtn" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String m = "Are you sure to delete Flume nodes installed on Hadoop cluster '%s'?";
                    ConfirmationDialog alert =
                            new ConfirmationDialog( String.format( m, config.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = flume.uninstallCluster( config );
                            ProgressWindow window =
                                    new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
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
    }


    private void getRefreshClusterButton( HorizontalLayout controlsContent )
    {
        Button refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "FluRefreshClusterBtn" );
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
    }


    private void getClusterCombo( HorizontalLayout controlsContent )
    {
        clusterCombo = new ComboBox();
        clusterCombo.setId( "FluClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( FlumeConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );

        controlsContent.addComponent( clusterCombo );
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


    private void refreshUI()
    {
        if ( config != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
            Set<ContainerHost> hosts = environment.getContainerHostsByIds( config.getNodes() );
            populateTable( nodesTable, hosts );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, Set<ContainerHost> containerHosts )
    {

        table.removeAllItems();

        for ( final ContainerHost host : containerHosts )
        {
            final Label resultHolder = new Label();
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.setId( host.getIpByInterfaceName( "eth0" ) + "-flumeDestroy" );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.setId( host.getIpByInterfaceName( "eth0" ) + "-flumeStart" );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.setId( host.getIpByInterfaceName( "eth0" ) + "-flumeStop" );

            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.setId( host.getIpByInterfaceName( "eth0" ) + "-flumeCheck" );

            enableButton( stopBtn, startBtn, checkBtn, destroyBtn );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );

            addStyleName( checkBtn, startBtn, stopBtn, destroyBtn, availableOperations );
            addGivenComponents( availableOperations, startBtn, stopBtn, checkBtn, destroyBtn );

            table.addItem( new Object[] {
                    host.getHostname(), host.getIpByInterfaceName( "eth0" ), resultHolder, availableOperations
            }, null );


            addCheckButtonClickListener( host, resultHolder, checkBtn, startBtn, stopBtn, destroyBtn );
            addClickListenerToStartButton( host, checkBtn, startBtn, stopBtn, destroyBtn );
            addClickListenerToStopButton( host, checkBtn, startBtn, stopBtn, destroyBtn );

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    String m = "Are you sure to remove Flume from node '%s'?";
                    ConfirmationDialog alert =
                            new ConfirmationDialog( String.format( m, host.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            // before destroying installation, stop flume process
                            stopBtn.click();
                            UUID trackID = flume.destroyNode( config.getClusterName(), host.getHostname() );
                            ProgressWindow window =
                                    new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
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
    }


    private void addClickListenerToStartButton( final ContainerHost host, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( flume, tracker, config.getClusterName(), host, NodeOperationType.START,
                                new org.safehaus.subutai.common.protocol.CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( NodeState nodeState )
                                    {
                                        synchronized ( PROGRESS_ICON )
                                        {
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                        }
                                    }
                                }, null ) );
            }
        } );
    }


    public void addClickListenerToStopButton( final ContainerHost host, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( flume, tracker, config.getClusterName(), host, NodeOperationType.STOP,
                                new org.safehaus.subutai.common.protocol.CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( NodeState nodeState )
                                    {
                                        synchronized ( PROGRESS_ICON )
                                        {
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                        }
                                    }
                                }, null ) );
            }
        } );
    }


    public void addCheckButtonClickListener( final ContainerHost host, final Label resultHolder,
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
                        new NodeOperationTask( flume, tracker, config.getClusterName(), host, NodeOperationType.STATUS,
                                new org.safehaus.subutai.common.protocol.CompleteEvent()
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
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).setEnabled( true );
                                            getButton( DESTROY_BUTTON_CAPTION, buttons ).setEnabled( true );
                                        }
                                    }
                                }, null ) );
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


    private void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    private void addGivenComponents( Layout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private void enableButton( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


    private void addStyleName( Component... components )
    {
        for ( Component c : components )
        {
            c.addStyleName( STYLE_NAME );
        }
    }


    public void refreshClustersInfo()
    {
        List<FlumeConfig> clustersInfo = flume.getClusters();
        FlumeConfig clusterInfo = ( FlumeConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( FlumeConfig ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                clusterCombo.setItemCaption( ci, ci.getClusterName() + "(" + ci.getHadoopClusterName() + ")" );
            }
            if ( clusterInfo != null )
            {
                for ( FlumeConfig ci : clustersInfo )
                {
                    if ( ci.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( ci );
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


    private void getClusterNameLabel( HorizontalLayout controlsContent )
    {
        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
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
                if ( event.isDoubleClick() )
                {
                    String containerId =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Set<ContainerHost> containerHosts =
                            environmentManager.getEnvironmentByUUID( config.getEnvironmentId() ).getContainerHosts();
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
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void disableButton( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
