/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.manager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
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
import com.vaadin.ui.Layout;
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
    protected static final String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String ADD_TRACER_BUTTON_CAPTION = "Add Tracer";
    protected static final String ADD_SLAVE_BUTTON_CAPTION = "Add Tablet Server";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String STYLE_NAME = "default";
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final GridLayout contentRoot;
    private final Table mastersTable;
    private final Table tracersTable;
    private final Table slavesTable;
    private final Pattern MASTER_PATTERN = Pattern.compile( ".*(Master.+?g).*" );
    private final Pattern GC_PATTERN = Pattern.compile( ".*(GC.+?g).*" );
    private final Pattern MONITOR_PATTERN = Pattern.compile( ".*(Monitor.+?g).*" );
    private final Pattern TRACER_PATTERN = Pattern.compile( ".*(Tracer.+?g).*" );
    private final Pattern LOGGER_PATTERN = Pattern.compile( ".*(Logger.+?g).*" );
    private final Pattern TABLET_SERVER_PATTERN = Pattern.compile( ".*(Tablet Server.+?g).*" );
    private final Accumulo accumulo;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final EnvironmentManager environmentManager;
    private ComboBox clusterCombo;
    private AccumuloClusterConfig accumuloClusterConfig;
    private Button refreshClustersBtn, checkAllBtn, startClusterBtn, stopClusterBtn, destroyClusterBtn, addTracerBtn,
            addTabletServerButton, addPropertyBtn, removePropertyBtn;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.accumulo = serviceLocator.getService( Accumulo.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );


        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 17 );
        contentRoot.setColumns( 1 );

        //tables go here
        mastersTable = createTableTemplate( "Masters" );
        mastersTable.setId( "MastersTable" );
        tracersTable = createTableTemplate( "Tracers" );
        tracersTable.setId( "TracersTable" );
        slavesTable = createTableTemplate( "Tablet Servers" );
        slavesTable.setId( "Slavestable" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );


        controlsContent.addComponent( getClusterCombo() );
        controlsContent.addComponent( getRefreshClustersButton() );
        controlsContent.addComponent( getCheckAllButton() );
        controlsContent.addComponent( getStartAllButton() );
        controlsContent.addComponent( getStopAllButton() );
        controlsContent.addComponent( getDestroyClusterButton() );
        controlsContent.addComponent( getAddTracerNodeButton() );
        controlsContent.addComponent( getAddSlaveButton() );

        HorizontalLayout customPropertyContent = getAddPropertyLayout();
        controlsContent.addComponent( customPropertyContent );

        addStyleName( refreshClustersBtn, startClusterBtn, stopClusterBtn, addTabletServerButton, addTracerBtn,
                addPropertyBtn, checkAllBtn, removePropertyBtn, destroyClusterBtn );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( customPropertyContent, 0, 1 );
        contentRoot.addComponent( mastersTable, 0, 2, 0, 6 );
        contentRoot.addComponent( tracersTable, 0, 7, 0, 11 );
        contentRoot.addComponent( slavesTable, 0, 12, 0, 16 );
    }


    private ComboBox getClusterCombo()
    {
        clusterCombo = new ComboBox();
        clusterCombo.setId( "ClusterCb" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                accumuloClusterConfig = ( AccumuloClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAll();
            }
        } );
        return clusterCombo;
    }


    private Button getRefreshClustersButton()
    {
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "ResfreshClustersBtn" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                refreshClustersInfo();
            }
        } );
        return refreshClustersBtn;
    }


    private Button getCheckAllButton()
    {
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "CheckAllBtn" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                checkAll();
            }
        } );
        return checkAllBtn;
    }


    private HorizontalLayout getAddPropertyLayout()
    {
        HorizontalLayout customPropertyContent = new HorizontalLayout();
        customPropertyContent.setSpacing( true );

        Label propertyNameLabel = new Label( "Property Name" );
        customPropertyContent.addComponent( propertyNameLabel );
        final TextField propertyNameTextField = new TextField();
        propertyNameTextField.setId( "propertyNameTxt" );
        customPropertyContent.addComponent( propertyNameTextField );

        removePropertyBtn = new Button( "Remove" );
        removePropertyBtn.setId( "removePropertyBtn" );
        removePropertyBtn.addStyleName( "default" );
        removePropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( accumuloClusterConfig != null )
                {
                    String propertyName = propertyNameTextField.getValue();
                    if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        Notification.show( "Please, specify property name to remove" );
                    }
                    else
                    {
                        UUID trackID = accumulo.removeProperty( accumuloClusterConfig.getClusterName(), propertyName );

                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( removePropertyBtn );

        Label propertyValueLabel = new Label( "Property Value" );
        customPropertyContent.addComponent( propertyValueLabel );
        final TextField propertyValueTextField = new TextField();
        propertyValueTextField.setId( "propertyValueTxt" );
        customPropertyContent.addComponent( propertyValueTextField );

        addPropertyBtn = new Button( "Add" );
        addPropertyBtn.setId( "addProperty" );
        addPropertyBtn.addStyleName( "default" );
        addPropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( accumuloClusterConfig != null )
                {
                    String propertyName = propertyNameTextField.getValue();
                    String propertyValue = propertyValueTextField.getValue();
                    if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        Notification.show( "Please, specify property name to add" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyValue ) )
                    {
                        Notification.show( "Please, specify property name to set" );
                    }
                    else
                    {
                        UUID trackID = accumulo.addProperty( accumuloClusterConfig.getClusterName(), propertyName,
                                propertyValue );

                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( addPropertyBtn );
        return customPropertyContent;
    }


    private Button getAddSlaveButton()
    {
        addTabletServerButton = new Button( ADD_SLAVE_BUTTON_CAPTION );
        addTabletServerButton.setId( "addTabletServer" );
        addTabletServerButton.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig == null )
                {
                    Notification.show( "Select cluster" );
                    return;
                }
                Set<UUID> set = new HashSet<>(
                        hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getAllNodes() );
                set.removeAll( accumuloClusterConfig.getAllNodes() );
                if ( set.isEmpty() )
                {
                    Notification.show( "All nodes in Hadoop cluster have Accumulo installed" );
                    return;
                }

                Set<ContainerHost> myHostSet = new HashSet<>();
                for ( UUID uuid : set )
                {
                    myHostSet.add( environmentManager.getEnvironmentByUUID(
                            hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getEnvironmentId() )
                                                     .getContainerHostByUUID( uuid ) );
                }

                AddNodeWindow w =
                        new AddNodeWindow( accumulo, executorService, tracker, accumuloClusterConfig, myHostSet,
                                NodeType.ACCUMULO_TABLET_SERVER );
                contentRoot.getUI().addWindow( w );
                w.addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                        refreshUI();
                        checkAll();
                    }
                } );
            }
        } );
        return addTabletServerButton;
    }


    private Button getAddTracerNodeButton()
    {
        addTracerBtn = new Button( ADD_TRACER_BUTTON_CAPTION );
        addTracerBtn.setId( "addTracer" );
        addTracerBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig == null )
                {
                    Notification.show( "Select cluster" );
                    return;
                }
                Set<UUID> set = new HashSet<>(
                        hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getAllNodes() );
                set.removeAll( accumuloClusterConfig.getAllNodes() );
                if ( set.isEmpty() )
                {
                    Notification.show( "All nodes in Hadoop cluster have Accumulo installed" );
                    return;
                }

                Set<ContainerHost> myHostSet = new HashSet<>();
                for ( UUID uuid : set )
                {
                    myHostSet.add( environmentManager.getEnvironmentByUUID(
                            hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getEnvironmentId() )
                                                     .getContainerHostByUUID( uuid ) );
                }

                AddNodeWindow w =
                        new AddNodeWindow( accumulo, executorService, tracker, accumuloClusterConfig, myHostSet,
                                NodeType.ACCUMULO_TRACER );
                contentRoot.getUI().addWindow( w );
                w.addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                        refreshUI();
                        checkAll();
                    }
                } );
            }
        } );
        return addTracerBtn;
    }


    private Button getDestroyClusterButton()
    {
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "destroyClusterBtn" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?",
                                    accumuloClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            stopClusterBtn.click();
                            UUID trackID = accumulo.uninstallCluster( accumuloClusterConfig.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        return destroyClusterBtn;
    }


    private Button getStartAllButton()
    {
        startClusterBtn = new Button( START_ALL_BUTTON_CAPTION );
        startClusterBtn.setId( "startAll" );
        startClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                PROGRESS_ICON.setVisible( true );
                executorService.execute(
                        new StartTask( accumulo, tracker, accumuloClusterConfig.getClusterName(), new CompleteEvent()
                        {
                            public void onComplete( String result )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    checkAll();
                                    PROGRESS_ICON.setVisible( false );
                                }
                            }
                        } ) );
            }
        } );
        return startClusterBtn;
    }


    private Button getStopAllButton()
    {
        stopClusterBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopClusterBtn.setId( "stopAll" );
        stopClusterBtn.addStyleName( "default" );
        stopClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                PROGRESS_ICON.setVisible( true );
                executorService.execute(
                        new StopTask( accumulo, tracker, accumuloClusterConfig.getClusterName(), new CompleteEvent()
                        {
                            public void onComplete( String result )
                            {
                                synchronized ( PROGRESS_ICON )
                                {
                                    checkAll();
                                    PROGRESS_ICON.setVisible( false );
                                }
                            }
                        } ) );
            }
        } );
        return stopClusterBtn;
    }


    public void checkAll()
    {
        checkNodesStatus( mastersTable );
        checkNodesStatus( tracersTable );
        checkNodesStatus( slavesTable );
    }


    private void checkNodesStatus( final Table nodesTable )
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
                String containerId =
                        ( String ) table.getItem( event.getItemId() ).getItemProperty( HOST_COLUMN_CAPTION ).getValue();
                ContainerHost containerHost = environmentManager.getEnvironmentByUUID(
                        hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getEnvironmentId() )
                                                                .getContainerHostByHostname( containerId );

                if ( containerHost != null )
                {
                    TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( containerHost ) );
                    contentRoot.getUI().addWindow( terminal.getWindow() );
                }
                else
                {
                    Notification.show( "Agent is not connected" );
                }
            }
        } );
    }


    private void refreshUI()
    {
        if ( accumuloClusterConfig != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID(
                    hadoop.getCluster( accumuloClusterConfig.getHadoopClusterName() ).getEnvironmentId() );
            populateTable( slavesTable, environment.getHostsByIds( accumuloClusterConfig.getSlaves() ), false );
            populateTable( tracersTable, environment.getHostsByIds( accumuloClusterConfig.getTracers() ), false );


            Set<ContainerHost> masters = new HashSet<>();
            masters.add( environment.getContainerHostByUUID( accumuloClusterConfig.getMasterNode() ) );
            masters.add( environment.getContainerHostByUUID( accumuloClusterConfig.getGcNode() ) );
            masters.add( environment.getContainerHostByUUID( accumuloClusterConfig.getMonitor() ) );
            populateTable( mastersTable, masters, true );
        }
        else
        {
            slavesTable.removeAllItems();
            tracersTable.removeAllItems();
            mastersTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, Set<ContainerHost> containerHosts, final boolean masters )
    {
        table.removeAllItems();
        for ( final ContainerHost containerHost : containerHosts )
        {
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            checkBtn.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-accumuloCheck" );
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.setId( containerHost.getIpByInterfaceName( "eth0" ) + "-accumuloDestroy" );
            final Label resultHolder = new Label();
            resultHolder.setId( containerHost.getIpByInterfaceName( "eth0" ) + "accumuloResult" );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );

            // TODO: think about adding destroy button !!!
            addGivenComponents( availableOperations, checkBtn );
            addStyleName( checkBtn, destroyBtn, availableOperations );

            final String nodeRole = findNodeRoles( containerHost );
            table.addItem( new Object[] {
                    containerHost.getHostname(), containerHost.getIpByInterfaceName( "eth0" ), nodeRole, resultHolder,
                    availableOperations
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    PROGRESS_ICON.setVisible( true );
                    disableButtons( checkBtn, destroyBtn );
                    executorService.execute( new CheckTask( accumulo, tracker, accumuloClusterConfig.getClusterName(),
                            containerHost.getHostname(), new CompleteEvent()
                    {
                        public void onComplete( String result )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                resultHolder.setValue( parseStatus( result, nodeRole ) );
                                enableButtons( destroyBtn, checkBtn );
                                PROGRESS_ICON.setVisible( false );
                            }
                        }
                    } ) );
                }
            } );

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s node?", containerHost.getHostname() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = accumulo.destroyNode( accumuloClusterConfig.getClusterName(),
                                    containerHost.getHostname(),
                                    table == tracersTable ? NodeType.ACCUMULO_TRACER : NodeType.ACCUMULO_LOGGER );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    AccumuloClusterConfig.PRODUCT_KEY );
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


    private void addStyleName( Component... components )
    {
        for ( Component c : components )
        {
            c.addStyleName( STYLE_NAME );
        }
    }


    private void addGivenComponents( Layout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private void enableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( true );
        }
    }


    private void disableButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.setEnabled( false );
        }
    }


    private String parseStatus( String result, String roles )
    {
        StringBuilder sb = new StringBuilder();

        if ( result.contains( "not connected" ) )
        {
            sb.append( "Agent is not connected !" );
            return sb.toString();
        }

        // A nodes has multiple role
        if ( roles.contains( "," ) )
        {
            String nodeRoles[] = roles.split( "," );
            for ( String role : nodeRoles )
            {
                parseNFillStringBuilder( result, role.trim(), sb );
            }
        }
        else
        {
            parseNFillStringBuilder( result, roles.trim(), sb );
        }
        if ( sb.length() > 0 )
        {
            return sb.toString().substring( 0, ( sb.length() - 2 ) );
        }
        return null;
    }


    private StringBuilder parseNFillStringBuilder( String result, String roles, StringBuilder sb )
    {
        switch ( roles )
        {
            case "Master":
                sb.append( parseStatus( result, MASTER_PATTERN ) ).append( ", " );
                break;
            case "GC":
                sb.append( parseStatus( result, GC_PATTERN ) ).append( ", " );
                break;
            case "Monitor":
                sb.append( parseStatus( result, MONITOR_PATTERN ) ).append( ", " );
                break;
            case "Tracer":
                sb.append( parseStatus( result, TRACER_PATTERN ) ).append( ", " );
                break;
            case "Logger":
                sb.append( parseStatus( result, LOGGER_PATTERN ) ).append( ", " );
                break;
            case "Tablet_Server":
                sb.append( parseStatus( result, TABLET_SERVER_PATTERN ) ).append( ", " );
                break;
        }
        return sb;
    }


    private String parseStatus( String result, Pattern pattern )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher masterMatcher = pattern.matcher( result );
        if ( masterMatcher.find() )
        {
            parsedResult.append( masterMatcher.group( 1 ) );
        }
        return parsedResult.toString();
    }


    private String findNodeRoles( ContainerHost node )
    {
        StringBuilder sb = new StringBuilder();
        if ( accumuloClusterConfig.getMasterNode().equals( node.getId() ) )
        {
            sb.append( "Master" ).append( ", " );
        }
        if ( accumuloClusterConfig.getGcNode().equals( node.getId() ) )
        {
            sb.append( "GC" ).append( ", " );
        }
        if ( accumuloClusterConfig.getMonitor().equals( node.getId() ) )
        {
            sb.append( "Monitor" ).append( ", " );
        }
        if ( accumuloClusterConfig.getTracers().contains( node.getId() ) )
        {
            sb.append( "Tracer" ).append( ", " );
        }
        if ( accumuloClusterConfig.getSlaves().contains( node.getId() ) )
        {
            sb.append( "Tablet_Server" ).append( ", " );
        }
        if ( sb.length() > 0 )
        {
            return sb.toString().substring( 0, ( sb.length() - 2 ) );
        }
        return null;
    }


    public void refreshClustersInfo()
    {
        List<AccumuloClusterConfig> mongoClusterInfos = accumulo.getClusters();
        AccumuloClusterConfig clusterInfo = ( AccumuloClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && !mongoClusterInfos.isEmpty() )
        {
            for ( AccumuloClusterConfig mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( AccumuloClusterConfig mongoClusterInfo : mongoClusterInfos )
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


    public Component getContent()
    {
        return contentRoot;
    }
}
