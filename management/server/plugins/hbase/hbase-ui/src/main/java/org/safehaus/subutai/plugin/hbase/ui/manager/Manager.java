/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.google.common.base.Preconditions;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

import static org.safehaus.subutai.plugin.hbase.api.HBaseType.HMaster;


public class Manager
{

    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTER_CAPTION = "Refresh Clusters";
    protected static final String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected static final String CHECK_BUTTON_CAPTION = "Check";
    protected static final String START_ALL_BUTTON_CAPTION = "Start All";
    protected static final String START_BUTTON_CAPTION = "Start";
    protected static final String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected static final String STOP_BUTTON_CAPTION = "Stop";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected static final String STATUS_COLUMN_CAPTION = "Status";
    protected static final String ADD_NODE_CAPTION = "Add Node";
    protected static final String TABLE_CAPTION = "All Nodes";
    protected static final String BUTTON_STYLE_NAME = "default";
    protected final Button refreshClustersBtn, startAllNodesBtn, stopAllNodesBtn, checkAllBtn, destroyClusterBtn;
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final ExecutorService executor;
    private final HBase hbase;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final String MESSAGE = "No cluster is installed !";
    private final Embedded PROGRESS_ICON = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private final Pattern HMASTER_PATTERN = Pattern.compile( ".*(HMaster.+?g).*" );
    private final Pattern REGION_PATTERN = Pattern.compile( ".*(HRegionServer.+?g).*" );
    private final Pattern QUORUM_PATTERN = Pattern.compile( ".*(HQuorumPeer.+?g).*" );
    private HBaseConfig config;
    private Table nodesTable = null;


    public Manager( final ExecutorService executor, final HBase hbase,final Hadoop hadoop, final Tracker tracker ) throws NamingException
    {
        Preconditions.checkNotNull( executor, "Executor is null" );

        this.hbase = hbase;
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.executor = executor;

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( TABLE_CAPTION );
        contentRoot.setId( "HbaseMngContentRoot" );
        nodesTable.setId( "HbaseMngNodesTable" );


        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
        controlsContent.setComponentAlignment( clusterNameLabel, Alignment.MIDDLE_CENTER );

        clusterCombo = new ComboBox();
        clusterCombo.setId( "HbaseMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                Object value = event.getProperty().getValue();
                config = value != null ? ( HBaseConfig ) value : null;
                refreshUI();
                checkAllNodes();
            }
        } );
        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );


        /** Refresh Cluster button */
        refreshClustersBtn = new Button( REFRESH_CLUSTER_CAPTION );
        refreshClustersBtn.setId( "HbaseMngRefresh" );
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
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        /** Check All button */
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllBtn.setId( "HbaseMngCheck" );
        checkAllBtn.addStyleName( BUTTON_STYLE_NAME );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( MESSAGE );
                }
                else
                {
                    checkAllNodes();
                }
            }
        } );
        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        /** Start All button */
        startAllNodesBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllNodesBtn.setId( "HbaseMngStart" );
        startAllNodesBtn.addStyleName( BUTTON_STYLE_NAME );
        startAllNodesBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
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
        controlsContent.addComponent( startAllNodesBtn );
        controlsContent.setComponentAlignment( startAllNodesBtn, Alignment.MIDDLE_CENTER );


        /** Stop All button */
        stopAllNodesBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllNodesBtn.setId( "HbaseMngStop" );
        stopAllNodesBtn.addStyleName( BUTTON_STYLE_NAME );
        stopAllNodesBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
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
        controlsContent.addComponent( stopAllNodesBtn );
        controlsContent.setComponentAlignment( stopAllNodesBtn, Alignment.MIDDLE_CENTER );


        /** Destroy All button */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId( "HbaseMngDestroy" );
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
                                    new ProgressWindow( executor, tracker, trackID, HBaseConfig.PRODUCT_KEY );
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
        controlsContent.setComponentAlignment( destroyClusterBtn, Alignment.MIDDLE_CENTER );

        PROGRESS_ICON.setVisible( false );
        PROGRESS_ICON.setId( "indicator" );
        controlsContent.addComponent( PROGRESS_ICON );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
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


    private void stopAllNodes()
    {
        PROGRESS_ICON.setVisible( true );
        disableOREnableAllButtonsOnTable( nodesTable, false );
        executor.execute( new StopTask( hbase, tracker, config.getClusterName(), new CompleteEvent()
        {
            @Override
            public void onComplete( String result )
            {
                synchronized ( PROGRESS_ICON )
                {
                    disableOREnableAllButtonsOnTable( nodesTable, true );
                    checkAllNodes();
                }
            }
        } ) );
    }


    private void startAllNodes()
    {
        PROGRESS_ICON.setVisible( true );
        startHadoopCluster();
        disableOREnableAllButtonsOnTable( nodesTable, false );
        executor.execute( new StartTask( hbase, tracker, config.getClusterName(), new CompleteEvent()
        {
            @Override
            public void onComplete( String result )
            {
                synchronized ( PROGRESS_ICON )
                {
                    disableOREnableAllButtonsOnTable( nodesTable, true );
                    checkAllNodes();
                }
            }
        } ) );
    }


    private void startHadoopCluster()
    {
        HadoopClusterConfig hadoopClusterConfig = hadoop.getCluster( config.getHadoopClusterName() );
        hadoop.startNameNode( hadoopClusterConfig );
    }


    private void disableOREnableAllButtonsOnTable( Table table, boolean value )
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


    private void populateTable( final Table table, Set<UUID> containerHosts )
    {
        for ( final UUID containerHost : containerHosts )
        {
            final Label resultHolder = new Label();
            final Button checkBtn = new Button( CHECK_BUTTON_CAPTION );
            //            checkBtn.setId( containerHost.getListIP().get( 0 ) + "-hbaseCheck" );
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
                    containerHost.toString(), /*containerHost.getListIP().get( 0 ),*/ findNodeRoles( containerHost ), resultHolder,
                    availableOperations
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
                    executor.execute( new CheckTask( hbase, tracker, config.getClusterName(), containerHost, new CompleteEvent()
                    {
                        public void onComplete( String result )
                        {
                            synchronized ( PROGRESS_ICON )
                            {
                                resultHolder.setValue( parseStatus( result, findNodeRoles( containerHost ) ) );
                                PROGRESS_ICON.setVisible( false );
                                checkBtn.setEnabled( true );
                            }
                        }
                    } ) );
                }
            } );
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
            populateTable( nodesTable, config.getAllNodes() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    private String findNodeRoles( UUID node )
    {
        StringBuilder sb = new StringBuilder();
        if ( config.getHbaseMaster() == node )
        {
            sb.append( HMaster.name() ).append( ", " );
        }
        if ( config.getRegionServers().contains( node ) )
        {
            sb.append( HBaseType.HRegionServer.name() ).append( ", " );
        }
        if ( config.getQuorumPeers().contains( node ) )
        {
            sb.append( HBaseType.HQuorumPeer.name() ).append( ", " );
        }
        if ( config.getBackupMasters().contains( node ) )
        {
            sb.append( HBaseType.BackupMaster.name() ).append( ", " );
        }
        if ( sb.length() > 0 )
        {
            return sb.toString().substring( 0, ( sb.length() - 2 ) );
        }
        return null;
    }


    public void refreshClustersInfo()
    {
        List<HBaseConfig> clusters = hbase.getClusters();
        HBaseConfig clusterInfo = ( HBaseConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clusters != null && !clusters.isEmpty() )
        {
            for ( HBaseConfig info : clusters )
            {
                clusterCombo.addItem( info );
                clusterCombo.setItemCaption( info, info.getClusterName() + "(" + info.getHadoopClusterName() + ")" );
            }
            if ( clusterInfo != null )
            {
                for ( HBaseConfig c : clusters )
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


    private String parseStatus( String result, String roles )
    {
        StringBuilder sb = new StringBuilder();

        if ( result.contains( "not connected" ) )
        {
            sb.append( "Host is not connected !" );
            return sb.toString();
        }

        // A nodes has multiple role
        if ( roles.contains( "," ) )
        {
            String nodeRoles[] = roles.split( "," );
            for ( String r : nodeRoles )
            {
                switch ( r.trim() )
                {
                    case "HMaster":
                        sb.append( parseStatus( result, HMASTER_PATTERN ) ).append( ", " );
                        break;
                    case "HRegionServer":
                        sb.append( parseStatus( result, REGION_PATTERN ) ).append( ", " );
                        break;
                    case "HQuorumPeer":
                        sb.append( parseStatus( result, QUORUM_PATTERN ) ).append( ", " );
                        break;
                    case "BackupMaster":
                        sb.append( parseStatus( result, HMASTER_PATTERN ) ).append( ", " );
                        break;
                }
            }
        }
        else
        {
            switch ( roles )
            {
                case "HMaster":
                    sb.append( parseStatus( result, HMASTER_PATTERN ) ).append( ", " );
                    break;
                case "HRegionServer":
                    sb.append( parseStatus( result, REGION_PATTERN ) ).append( ", " );
                    break;
                case "HQuorumPeer":
                    sb.append( parseStatus( result, QUORUM_PATTERN ) ).append( ", " );
                    break;
                case "BackupMaster":
                    sb.append( parseStatus( result, HMASTER_PATTERN ) ).append( ", " );
                    break;
            }
        }


        return sb.toString().substring( 0, ( sb.length() - 2 ) );
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
                    //                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    //                    if ( lxcAgent != null )
                    //                    {
                       /* TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executor, commandRunner,
                                        agentManager );
                        contentRoot.getUI().addWindow( terminal.getWindow() );*/
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


    public Component getContent()
    {
        return contentRoot;
    }
}
