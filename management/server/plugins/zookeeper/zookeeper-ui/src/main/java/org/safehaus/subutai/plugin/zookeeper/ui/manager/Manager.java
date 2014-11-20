/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.manager;


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
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.base.Strings;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;


public class Manager
{

    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String START_ALL_BUTTON_CAPTION = "Start All";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String BUTTON_STYLE_NAME = "default";
    private static final String MESSAGE = "No cluster is installed !";
    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn, addNodeBtn;
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final Hadoop hadoop;
    private final Zookeeper zookeeper;
    private final CommandRunner commandRunner;
    private final EnvironmentManager environmentManager;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private ZookeeperClusterConfig config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.zookeeper = serviceLocator.getService( Zookeeper.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "ZookeeperMngNodesTable" );
        contentRoot.setId( "ZookeeperMngContentRoot" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "ZookeeperMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( ZookeeperClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkNodesStatus();
            }
        } );
        controlsContent.addComponent( clusterCombo );

        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "ZookeeperMngRefresh" );
        addClickListener( refreshClustersBtn );
        controlsContent.addComponent( refreshClustersBtn );

        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "ZookeeperMngCheckAll" );
        addClickListener( checkAllBtn );
        controlsContent.addComponent( checkAllBtn );

        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.setId( "ZookeeperMngStartAll" );
        addClickListener( startAllBtn );
        controlsContent.addComponent( startAllBtn );

        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.setId( "ZookeeperMngStopAll" );
        addClickListener( stopAllBtn );
        controlsContent.addComponent( stopAllBtn );

        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "ZookeeperMngDestroyCluster" );
        addClickListenerToDestroyButton();
        controlsContent.addComponent( destroyClusterBtn );

        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId( "ZookeeperMngAddNode" );
        addClickListenerToAddNodeButton();
        controlsContent.addComponent( addNodeBtn );

        addStyleNameToButtons( refreshClustersBtn, checkAllBtn, startAllBtn, stopAllBtn, destroyClusterBtn,
                addNodeBtn );


        HorizontalLayout customPropertyContent = new HorizontalLayout();
        customPropertyContent.setSpacing( true );

        Label fileLabel = new Label( "File" );
        customPropertyContent.addComponent( fileLabel );
        final TextField fileTextField = new TextField();
        fileTextField.setId( "ZookeeperMngFileName" );
        customPropertyContent.addComponent( fileTextField );
        Label propertyNameLabel = new Label( "Property Name" );
        customPropertyContent.addComponent( propertyNameLabel );
        final TextField propertyNameTextField = new TextField();
        propertyNameTextField.setId( "ZookeeperMngPropertyName" );
        customPropertyContent.addComponent( propertyNameTextField );

        Button removePropertyBtn = new Button( "Remove" );
        removePropertyBtn.setId( "ZookeeperMngRemove" );
        removePropertyBtn.addStyleName( "default" );
        removePropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String fileName = fileTextField.getValue();
                    String propertyName = propertyNameTextField.getValue();
                    if ( Strings.isNullOrEmpty( fileName ) )
                    {
                        show( "Please, specify file name where property resides" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        show( "Please, specify property name to remove" );
                    }
                    else
                    {
                        UUID trackID = zookeeper.removeProperty( config.getClusterName(), fileName, propertyName );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                ZookeeperClusterConfig.PRODUCT_KEY );
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
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( removePropertyBtn );

        Label propertyValueLabel = new Label( "Property Value" );
        customPropertyContent.addComponent( propertyValueLabel );
        final TextField propertyValueTextField = new TextField();
        propertyValueTextField.setId( "ZookeeperMngPropertyValue" );
        customPropertyContent.addComponent( propertyValueTextField );
        Button addPropertyBtn = new Button( "Add" );
        addPropertyBtn.setId( "ZookeeperMngAdd" );
        addPropertyBtn.addStyleName( "default" );
        addPropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String fileName = fileTextField.getValue();
                    String propertyName = propertyNameTextField.getValue();
                    String propertyValue = propertyValueTextField.getValue();
                    if ( Strings.isNullOrEmpty( fileName ) )
                    {
                        show( "Please, specify file name where property will be added" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        show( "Please, specify property name to add" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyValue ) )
                    {
                        show( "Please, specify property value to set" );
                    }
                    else
                    {
                        UUID trackID =
                                zookeeper.addProperty( config.getClusterName(), fileName, propertyName, propertyValue );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                ZookeeperClusterConfig.PRODUCT_KEY );
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
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( addPropertyBtn );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( customPropertyContent, 0, 1 );
        contentRoot.addComponent( nodesTable, 0, 2, 0, 10 );
    }


    public void addClickListener( Button button )
    {
        if ( button.getCaption().equals( REFRESH_CLUSTERS_CAPTION ) )
        {
            button.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    refreshClustersInfo();
                }
            } );
            return;
        }
        switch ( button.getCaption() )
        {
            case CHECK_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            checkNodesStatus();
                        }
                    }
                } );
                break;

            case START_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            startAllNodes();
                        }
                    }
                } );
                break;
            case STOP_ALL_BUTTON_CAPTION:
                button.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent event )
                    {
                        if ( config == null )
                        {
                            show( MESSAGE );
                        }
                        else
                        {
                            stopAllNodes();
                        }
                    }
                } );
                break;
        }
    }


    public void startAllNodes()
    {

        Set<Agent> agentSet = new HashSet<>();
        Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
        for ( UUID agentID : config.getNodes() ) {
            agentSet.add( environment.getContainerHostByUUID( agentID ).getAgent() );
        }
        for ( Agent agent : agentSet )
        {
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new StartTask( zookeeper, tracker, config.getClusterName(), agent.getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkNodesStatus();
                            }
                        }
                    } ) );
        }
    }


    public void stopAllNodes()
    {
        Set<Agent> agentSet = new HashSet<>();
        Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
        for ( UUID agentID : config.getNodes() ) {
            agentSet.add( environment.getContainerHostByUUID( agentID ).getAgent() );
        }

        for ( Agent agent : agentSet )
        {
            PROGRESS_ICON.setVisible( true );
            disableOREnableAllButtonsOnTable( nodesTable, false );
            executorService.execute(
                    new StopTask( zookeeper, tracker, config.getClusterName(), agent.getHostname(), new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                disableOREnableAllButtonsOnTable( nodesTable, true );
                                checkNodesStatus();
                            }
                        }
                    } ) );
        }
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


    public void addClickListenerToDestroyButton()
    {
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
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
                            UUID trackID = zookeeper.uninstallCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    ZookeeperClusterConfig.PRODUCT_KEY );
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
    }


    public void addClickListenerToAddNodeButton()
    {
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( config != null )
                {
                    if ( config.getSetupType() == SetupType.STANDALONE )
                    {
                        ConfirmationDialog alert = new ConfirmationDialog(
                                String.format( "Do you want to add node to the %s cluster?", config.getClusterName() ),
                                "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                UUID trackID = zookeeper.addNode( config.getClusterName() );
                                ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                        ZookeeperClusterConfig.PRODUCT_KEY );
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
                    else if ( config.getSetupType() == SetupType.OVER_HADOOP
                            || config.getSetupType() == SetupType.WITH_HADOOP )
                    {
                        HadoopClusterConfig info = hadoop.getCluster( config.getHadoopClusterName() );

                        if ( info != null )
                        {
                            Set<Agent> nodes = new HashSet<>( info.getAllNodes() );
                            nodes.removeAll( config.getNodes() );
                            if ( !nodes.isEmpty() )
                            {
                                AddNodeWindow addNodeWindow =
                                        new AddNodeWindow( zookeeper, executorService, tracker, config, nodes );
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
                                show( "All nodes in corresponding Hadoop cluster have Zookeeper installed" );
                            }
                        }
                        else
                        {
                            show( "Hadoop cluster info not found" );
                        }
                    }
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
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

        addItemClickListenerToTable( table );
        return table;
    }


    public void addItemClickListenerToTable( final Table table )
    {
        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( HOST_COLUMN_CAPTION )
                                            .getValue();
                    Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
                    ContainerHost containerHost = environment.getContainerHostByHostname( lxcHostname );
                    if ( containerHost != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( containerHost ) );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void refreshUI()
    {
        Environment environment = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() );
        if ( config != null )
        {
            populateTable( nodesTable, environment.getContainers() );
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
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            final Button startBtn = new Button( START_BUTTON_CAPTION );
            final Button stopBtn = new Button( STOP_BUTTON_CAPTION );
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );

            checkBtn.setId( containerHost.getAgent().getListIP().get( 0 ) + "-zookeeperCheck" );
            startBtn.setId( containerHost.getAgent().getListIP().get( 0 ) + "-zookeeperStart" );
            stopBtn.setId( containerHost.getAgent().getListIP().get( 0 ) + "-zookeeperStop" );
            destroyBtn.setId( containerHost.getAgent().getListIP().get( 0 ) + "-zookeeperDestroy" );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            addGivenComponents( availableOperations, checkBtn, startBtn, stopBtn, destroyBtn );
            addStyleNameToButtons( checkBtn, startBtn, stopBtn, destroyBtn );

            disableButtons( stopBtn, startBtn );
            PROGRESS_ICON.setVisible( false );

            table.addItem( new Object[] {
                    containerHost.getHostname(), containerHost.getAgent().getListIP().get( 0 ), resultHolder, availableOperations
            }, null );


            addCheckButtonClickListener( containerHost, resultHolder, startBtn, stopBtn, destroyBtn, checkBtn );
            addStartButtonClickListener( containerHost, startBtn, stopBtn, destroyBtn, checkBtn );
            addStopButtonClickListener( containerHost, startBtn, stopBtn, destroyBtn, checkBtn );
            addDestroyButtonClickListener( containerHost, destroyBtn );
        }
    }


    public void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    public void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    public void addDestroyButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( DESTROY_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", containerHost.getHostname() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID = zookeeper.destroyNode( config.getClusterName(), containerHost.getHostname() );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                ZookeeperClusterConfig.PRODUCT_KEY );
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


    public void refreshClustersInfo()
    {
        List<ZookeeperClusterConfig> mongoClusterInfos = zookeeper.getClusters();
        ZookeeperClusterConfig clusterInfo = ( ZookeeperClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && !mongoClusterInfos.isEmpty() )
        {
            for ( ZookeeperClusterConfig mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( ZookeeperClusterConfig mongoClusterInfo : mongoClusterInfos )
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


    public void addStopButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( STOP_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute( new StopTask( zookeeper, tracker, config.getClusterName(), containerHost.getHostname(),
                        new CompleteEvent()
                        {
                            @Override
                            public void onComplete( String result )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    enableButtons( buttons );
                                    getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                }
                            }
                        } ) );
            }
        } );
    }


    public void addStartButtonClickListener( final ContainerHost containerHost, final Button... buttons )
    {
        getButton( START_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new StartTask( zookeeper, tracker, config.getClusterName(), containerHost.getHostname(),
                                new CompleteEvent()
                                {
                                    @Override
                                    public void onComplete( String result )
                                    {
                                        synchronized ( PROGRESS_ICON )
                                        {
                                            enableButtons( buttons );
                                            getButton( CHECK_BUTTON_CAPTION, buttons ).click();
                                        }
                                    }
                                } ) );
            }
        } );
    }


    public void addCheckButtonClickListener( final ContainerHost containerHost, final Label resultHolder, final Button... buttons )
    {
        getButton( CHECK_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                PROGRESS_ICON.setVisible( true );
                disableButtons( buttons );
                executorService.execute(
                        new CheckTask( zookeeper, tracker, config.getClusterName(), containerHost.getHostname(),
                                new CompleteEvent()
                                {
                                    public void onComplete( String result )
                                    {
                                        synchronized ( PROGRESS_ICON )
                                        {
                                            resultHolder.setValue( result );
                                            if ( resultHolder.getValue().toLowerCase().contains( "not" ) )
                                            {
                                                enableButtons( buttons );
                                                getButton( START_BUTTON_CAPTION, buttons ).setEnabled( true );
                                                getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( false );
                                            }
                                            else
                                            {
                                                enableButtons( buttons );
                                                getButton( START_BUTTON_CAPTION, buttons ).setEnabled( false );
                                                getButton( STOP_BUTTON_CAPTION, buttons ).setEnabled( true );
                                            }
                                            PROGRESS_ICON.setVisible( false );
                                        }
                                    }
                                } ) );
            }
        } );
    }


    public void enableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


    public Button getButton( String caption, Button... buttons )
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


    public void addGivenComponents( HorizontalLayout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    public void checkNodesStatus()
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


    public Component getContent()
    {
        return contentRoot;
    }
}
