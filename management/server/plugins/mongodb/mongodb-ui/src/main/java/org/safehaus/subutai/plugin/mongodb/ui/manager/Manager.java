/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

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


/**
 * @author dilshat
 */
public class Manager
{

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table configServersTable;
    private final Table routersTable;
    private final Table dataNodesTable;
    private final Label replicaSetName;
    private final Label domainName;
    private final Label cfgSrvPort;
    private final Label routerPort;
    private final Label dataNodePort;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final Mongo mongo;
    private MongoClusterConfig mongoClusterConfig;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.mongo = serviceLocator.getService( Mongo.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        configServersTable = createTableTemplate( "Config Servers" );
        configServersTable.setId( "MongoConfigsserversTbl" );
        routersTable = createTableTemplate( "Query Routers" );
        routersTable.setId( "MongoRoutersTbl" );
        dataNodesTable = createTableTemplate( "Data Nodes" );
        dataNodesTable.setId( "MongoDataNodesTbl" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "MongoClusterCb" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                mongoClusterConfig = ( MongoClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.setId( "MongoRefreshClustersBtn" );
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

        Button checkAllBtn = new Button( "Check all" );
        checkAllBtn.setId( "MongoCheckAllBtn" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                checkNodesStatus( configServersTable );
                checkNodesStatus( routersTable );
                checkNodesStatus( dataNodesTable );
            }
        } );
        controlsContent.addComponent( checkAllBtn );

        Button startAllBtn = new Button( "Start all" );
        startAllBtn.setId( "MongoStartAllBtn" );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                startAllNodes( configServersTable );
                startAllNodes( routersTable );
                startAllNodes( dataNodesTable );
            }
        } );
        controlsContent.addComponent( startAllBtn );

        Button stopAllBtn = new Button( "Stop all" );
        stopAllBtn.setId( "MongoStopAllBtn" );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                stopAllNodes( configServersTable );
                stopAllNodes( routersTable );
                stopAllNodes( dataNodesTable );
            }
        } );
        controlsContent.addComponent( stopAllBtn );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.setId( "MongoDestroyClusterBtn" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( mongoClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?",
                                    mongoClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = mongo.uninstallCluster( mongoClusterConfig.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    MongoClusterConfig.PRODUCT_KEY );
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

        Button addRouterBtn = new Button( "Add Router" );
        addRouterBtn.setId( "MongoAddRouterBtn" );
        addRouterBtn.addStyleName( "default" );
        addRouterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( mongoClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add ROUTER to the %s cluster?",
                                    mongoClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = mongo.addNode( mongoClusterConfig.getClusterName(), NodeType.ROUTER_NODE );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    MongoClusterConfig.PRODUCT_KEY );
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

        Button addDataNodeBtn = new Button( "Add Data Node" );
        addDataNodeBtn.setId( "MongoAddNodeBtn" );
        addDataNodeBtn.addStyleName( "default" );
        addDataNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( mongoClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add DATA_NODE to the %s cluster?",
                                    mongoClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = mongo.addNode( mongoClusterConfig.getClusterName(), NodeType.DATA_NODE );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    MongoClusterConfig.PRODUCT_KEY );
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

        controlsContent.addComponent( addRouterBtn );
        controlsContent.addComponent( addDataNodeBtn );

        HorizontalLayout configContent = new HorizontalLayout();
        configContent.setSpacing( true );

        replicaSetName = new Label();
        domainName = new Label();
        cfgSrvPort = new Label();
        routerPort = new Label();
        dataNodePort = new Label();

        configContent.addComponent( new Label( "Replica Set:" ) );
        configContent.addComponent( replicaSetName );
        configContent.addComponent( new Label( "Domain:" ) );
        configContent.addComponent( domainName );
        configContent.addComponent( new Label( "Config server port:" ) );
        configContent.addComponent( cfgSrvPort );
        configContent.addComponent( new Label( "Router port:" ) );
        configContent.addComponent( routerPort );
        configContent.addComponent( new Label( "Data node port:" ) );
        configContent.addComponent( dataNodePort );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( configContent, 0, 1 );
        contentRoot.addComponent( configServersTable, 0, 2, 0, 4 );
        contentRoot.addComponent( routersTable, 0, 5, 0, 7 );
        contentRoot.addComponent( dataNodesTable, 0, 8, 0, 10 );
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
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
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    //TODO please use ContainerHost.isConnected method here to check of host is connected
                    //                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    //                    if ( lxcAgent != null )
                    //                    {
                    //                        //                        TerminalWindow terminal =
                    //                        //                                new TerminalWindow( Sets.newHashSet(
                    // lxcAgent ),
                    //                        // executorService, commandRunner,
                    //                        //                                        agentManager );
                    //                        //                        contentRoot.getUI().addWindow( terminal
                    // .getWindow() );
                    //                    }
                    //                    else
                    //                    {
                    //                        show( "Agent is not connected" );
                    //                    }
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
        if ( mongoClusterConfig != null )
        {
            populateTable( configServersTable, mongoClusterConfig.getConfigServers(), NodeType.CONFIG_NODE );
            populateTable( routersTable, mongoClusterConfig.getRouterServers(), NodeType.ROUTER_NODE );
            populateTable( dataNodesTable, mongoClusterConfig.getDataNodes(), NodeType.DATA_NODE );
            replicaSetName.setValue( mongoClusterConfig.getReplicaSetName() );
            domainName.setValue( mongoClusterConfig.getDomainName() );
            cfgSrvPort.setValue( mongoClusterConfig.getCfgSrvPort() + "" );
            routerPort.setValue( mongoClusterConfig.getRouterPort() + "" );
            dataNodePort.setValue( mongoClusterConfig.getDataNodePort() + "" );
        }
        else
        {
            configServersTable.removeAllItems();
            routersTable.removeAllItems();
            dataNodesTable.removeAllItems();
            replicaSetName.setValue( "" );
            domainName.setValue( "" );
            cfgSrvPort.setValue( "" );
            routerPort.setValue( "" );
            dataNodePort.setValue( "" );
        }
    }


    private void populateTable( final Table table, Set nodes, final NodeType nodeType )
    {

        table.removeAllItems();

        for ( final Object o : nodes )
        {
            final MongoNode node = ( MongoNode ) o;
            final Button checkBtn = new Button( "Check" );
            checkBtn.setId( node.getContainerHost().getIpByInterfaceName( "eth0" ) + "-mongoCheck" );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( "Start" );
            startBtn.setId( node.getContainerHost().getIpByInterfaceName( "eth0" ) + "-mongoStart" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.setId( node.getContainerHost().getIpByInterfaceName( "eth0" ) + "mongoStop" );
            stopBtn.addStyleName( "default" );
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.setId( node.getContainerHost().getIpByInterfaceName( "eth0" ) + "mongoDestroy" );
            destroyBtn.addStyleName( "default" );
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            progressIcon.setId( node.getContainerHost().getIpByInterfaceName( "eth0" ) + "mongoProgress" );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );

            table.addItem( new Object[] {
                    node.getHostname(), checkBtn, startBtn, stopBtn, destroyBtn, progressIcon
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executorService.execute(
                            new CheckTask( mongo, tracker, mongoClusterConfig.getClusterName(), node.getHostname(),
                                    new CompleteEvent()
                                    {

                                        public void onComplete( NodeState state )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                if ( state == NodeState.RUNNING )
                                                {
                                                    stopBtn.setEnabled( true );
                                                }
                                                else if ( state == NodeState.STOPPED )
                                                {
                                                    startBtn.setEnabled( true );
                                                }
                                                destroyBtn.setEnabled( true );
                                                progressIcon.setVisible( false );
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

                    executorService.execute(
                            new StartTask( mongo, tracker, nodeType, mongoClusterConfig.getClusterName(),
                                    node.getHostname(), new CompleteEvent()
                            {

                                public void onComplete( NodeState state )
                                {
                                    synchronized ( progressIcon )
                                    {
                                        if ( state == NodeState.RUNNING )
                                        {
                                            stopBtn.setEnabled( true );
                                        }
                                        else
                                        {
                                            startBtn.setEnabled( true );
                                        }
                                        destroyBtn.setEnabled( true );
                                        progressIcon.setVisible( false );
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

                    executorService.execute(
                            new StopTask( mongo, tracker, mongoClusterConfig.getClusterName(), node.getHostname(),
                                    new CompleteEvent()
                                    {

                                        public void onComplete( NodeState state )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                if ( state == NodeState.STOPPED )
                                                {
                                                    startBtn.setEnabled( true );
                                                }
                                                else
                                                {
                                                    stopBtn.setEnabled( true );
                                                }
                                                destroyBtn.setEnabled( true );
                                                progressIcon.setVisible( false );
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
                            String.format( "Do you want to destroy the %s node?", node.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = mongo.destroyNode( mongoClusterConfig.getClusterName(), node.getHostname() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    MongoClusterConfig.PRODUCT_KEY );
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


    public void refreshClustersInfo()
    {
        List<MongoClusterConfig> mongoClusterInfos = mongo.getClusters();
        MongoClusterConfig clusterInfo = ( MongoClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && !mongoClusterInfos.isEmpty() )
        {
            for ( MongoClusterConfig mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( MongoClusterConfig mongoClusterInfo : mongoClusterInfos )
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


    public static void startAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Start" ).getValue() );
            checkBtn.click();
        }
    }


    public static void stopAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Stop" ).getValue() );
            checkBtn.click();
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
