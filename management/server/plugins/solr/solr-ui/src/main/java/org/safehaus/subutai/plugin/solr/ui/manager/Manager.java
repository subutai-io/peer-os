/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.manager;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.solr.api.NodeOperationTask;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;

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
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String DESTROY_INSTALLATION_BUTTON_CAPTION = "Destroy Installation";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String BUTTON_STYLE_NAME = "default";
    private static final String MESSAGE = "No cluster is installed !";
    final Button refreshClustersBtn, destroyInstallationBtn;
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;
    private final Solr solr;
    private SolrClusterConfig solrClusterConfig;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.solr = serviceLocator.getService( Solr.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "SlrNodeTbl" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the installation" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "SlrClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                solrClusterConfig = ( SolrClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );
        controlsContent.addComponent( clusterCombo );


        /**  Refresh clusters button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "SlrRefreshClustersBtn" );
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


        /**  Destroy cluster button  */
        destroyInstallationBtn = new Button( DESTROY_INSTALLATION_BUTTON_CAPTION );
        destroyInstallationBtn.setId( "SolrDestroyCluster" );
        destroyInstallationBtn.addStyleName( "default" );
        addClickListenerToDestroyInstallationButton();
        controlsContent.addComponent( destroyInstallationBtn );

        addStyleNameToButtons( refreshClustersBtn, destroyInstallationBtn );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private void addClickListenerToDestroyInstallationButton()
    {
        destroyInstallationBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( solrClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s installation?",
                                    solrClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = solr.uninstallCluster( solrClusterConfig );
                            final ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    SolrClusterConfig.PRODUCT_KEY );
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
                    show( MESSAGE );
                }
            }
        } );
    }


    private void refreshUI()
    {
        if ( solrClusterConfig != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( solrClusterConfig.getEnvironmentId() );
            populateTable( nodesTable, environment.getContainerHosts() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, Set<ContainerHost> containerHosts )
    {

        table.removeAllItems();

        for ( final ContainerHost containerHost : containerHosts )
        {
            final Label resultHolder = new Label();
            resultHolder.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-solrResult" );
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-solrCheck" );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            startBtn.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-solrStart" );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            stopBtn.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-solrStop" );

            addStyleNameToButtons( checkBtn, startBtn, stopBtn );
            enableButtons( startBtn, stopBtn );
            PROGRESS_ICON.setVisible( false );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn );

            disableButtons( startBtn, stopBtn );
            PROGRESS_ICON.setVisible( false );


            table.addItem( new Object[] {
                    containerHost.getHostname(), containerHost.getIpByInterfaceName( "eth0" ), resultHolder, availableOperations
            }, null );

            addCheckButtonClickListener( containerHost, resultHolder, startBtn, stopBtn, checkBtn );
            addStartButtonClickListener( containerHost, startBtn, stopBtn, checkBtn );
            addStopButtonClickListener( containerHost, startBtn, stopBtn, checkBtn );
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


    private void enableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


    private void addGivenComponents( Layout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private void addCheckButtonClickListener( final ContainerHost containerHost, final Label resultHolder, final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( solr, tracker, solrClusterConfig.getClusterName(), containerHost,
                                NodeOperationType.STATUS, new org.safehaus.subutai.common.protocol.CompleteEvent()
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


    private void addStopButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( solr, tracker, solrClusterConfig.getClusterName(), containerHost,
                                NodeOperationType.STOP, new org.safehaus.subutai.common.protocol.CompleteEvent()
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


    private void addStartButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new NodeOperationTask( solr, tracker, solrClusterConfig.getClusterName(), containerHost,
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


    public void refreshClustersInfo()
    {
        List<SolrClusterConfig> mongoClusterInfos = solr.getClusters();
        SolrClusterConfig clusterInfo = ( SolrClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && !mongoClusterInfos.isEmpty() )
        {
            for ( SolrClusterConfig mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( SolrClusterConfig mongoClusterInfo : mongoClusterInfos )
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
                clusterCombo.setValue( mongoClusterInfos.iterator().next() );
            }
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
                            environmentManager.getEnvironmentByUUID( solrClusterConfig.getEnvironmentId() ).getContainerHosts();
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
        return table;
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    public void checkStatus()
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


    private Button getButton( final HorizontalLayout availableOperationsLayout, String caption )
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
