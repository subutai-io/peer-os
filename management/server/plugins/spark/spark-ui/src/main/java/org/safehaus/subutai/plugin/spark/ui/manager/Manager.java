package org.safehaus.subutai.plugin.spark.ui.manager;


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
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.base.Preconditions;
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
import com.vaadin.ui.Window;


public class Manager
{

    public static final String MASTER_PREFIX = "Master: ";
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ExecutorService executor;
    private final Spark spark;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final String message = "No cluster is installed !";
    private final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private SparkClusterConfig config;


    public Manager( final ExecutorService executor, final ServiceLocator serviceLocator ) throws NamingException
    {
        Preconditions.checkNotNull( executor, "Executor is null" );
        Preconditions.checkNotNull( serviceLocator, "Service Locator is null" );

        this.spark = serviceLocator.getService( Spark.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );

        this.executor = executor;
        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
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
                config = ( SparkClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkAllNodesStatus();
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

        Button checkAllBtn = new Button( "Check All" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    checkAllNodesStatus();
                }
            }
        } );
        controlsContent.addComponent( checkAllBtn );

        Button startAllNodesBtn = new Button( "Start All" );
        startAllNodesBtn.addStyleName( "default" );
        startAllNodesBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    startAllNodes();
                }
            }
        } );
        controlsContent.addComponent( startAllNodesBtn );

        final Button stopAllNodesBtn = new Button( "Stop All" );
        stopAllNodesBtn.addStyleName( "default" );
        stopAllNodesBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( message );
                }
                else
                {
                    stopAllNodes();
                }
            }
        } );
        controlsContent.addComponent( stopAllNodesBtn );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
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
                            stopAllNodesBtn.click();
                            UUID trackID = spark.uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( executor, tracker, trackID, SparkClusterConfig.PRODUCT_KEY );
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

        Button addNodeBtn = new Button( "Add Node" );
        addNodeBtn.addStyleName( "default" );
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

                Set<Agent> set = null;
                if ( config.getSetupType() == SetupType.OVER_HADOOP )
                {
                    String hn = config.getHadoopClusterName();
                    if ( hn != null && !hn.isEmpty() )
                    {
                        HadoopClusterConfig hci = hadoop.getCluster( hn );
                        if ( hci != null )
                        {
                            set = new HashSet<>( hci.getAllNodes() );
                        }
                    }
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                {
                    set = new HashSet<>( config.getHadoopNodes() );
                }

                if ( set == null )
                {
                    show( "Hadoop cluster not found" );
                    return;
                }
                set.removeAll( config.getAllNodes() );
                if ( set.isEmpty() )
                {
                    show( "All nodes in Hadoop cluster have Hive installed" );
                    return;
                }

                AddNodeWindow w = new AddNodeWindow( executor, spark, tracker, config, set );
                contentRoot.getUI().addWindow( w );
                w.addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        refreshClustersInfo();
                    }
                } );
            }
        } );

        controlsContent.addComponent( addNodeBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "IP", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        //        table.addContainerProperty( "Action", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        table.setColumnCollapsingAllowed( true );
        table.setColumnCollapsed( "Check", true );

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
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executor, commandRunner,
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


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( nodesTable, config.getSlaveNodes(), config.getMasterNode() );
            checkAllNodesStatus();
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void checkAllNodesStatus()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.click();
        }
    }


    private void populateTable( final Table table, Set<Agent> agents, final Agent master )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Button checkBtn = new Button( "Check" );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( "Start" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.addStyleName( "default" );

            //            final Button setMasterBtn = new Button( "Set As Master" );
            //            setMasterBtn.addStyleName( "default" );

            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.addStyleName( "default" );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );

            table.addItem( new Object[] {
                    agent.getHostname(), String.format( " (%s)", agent.getListIP().get( 0 ) ), checkBtn, startBtn,
                    stopBtn, /* master.equals( agent ) ? null : setMasterBtn,*/ destroyBtn
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    //                    setMasterBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executor.execute(
                            new CheckTask( tracker, spark, config.getClusterName(), agent.getHostname(), false,
                                    new CompleteEvent()
                                    {

                                        @Override
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
                                                //                                                setMasterBtn
                                                // .setEnabled( true );
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
                    //                    setMasterBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executor.execute(
                            new StartTask( tracker, spark, config.getClusterName(), agent.getHostname(), false,
                                    new CompleteEvent()
                                    {

                                        @Override
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
                                                //                                                setMasterBtn
                                                // .setEnabled( true );
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
                    //                    setMasterBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    executor.execute( new StopTask( tracker, spark, config.getClusterName(), agent.getHostname(), false,
                            new CompleteEvent()
                            {

                                @Override
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
                                        //                                                setMasterBtn.setEnabled(
                                        // true );
                                        destroyBtn.setEnabled( true );
                                        progressIcon.setVisible( false );
                                    }
                                }
                            } ) );
                }
            } );

            //            setMasterBtn.addClickListener( new Button.ClickListener() {
            //
            //                @Override
            //                public void buttonClick( Button.ClickEvent event ) {
            //                    ConfirmationDialog alert = new ConfirmationDialog(
            //                            String.format( "Do you want to set %s as master node?",
            // agent.getHostname() ), "Yes",
            //                            "No" );
            //                    alert.getOk().addClickListener( new Button.ClickListener() {
            //                        @Override
            //                        public void buttonClick( Button.ClickEvent clickEvent ) {
            //                            ConfirmationDialog alert =
            //                                    new ConfirmationDialog( "Do you want to have a slave on the master
            // node?", "Yes",
            //                                            "No" );
            //                            alert.getOk().addClickListener( new Button.ClickListener() {
            //                                @Override
            //                                public void buttonClick( Button.ClickEvent clickEvent ) {
            //                                    UUID trackID = spark.changeMasterNode( config.getClusterName(),
            // agent.getHostname(),
            //                                            true );
            //                                    ProgressWindow window = new ProgressWindow( executor, tracker,
            // trackID,
            //                                            SparkClusterConfig.PRODUCT_KEY );
            //                                    window.getWindow().addCloseListener( new Window.CloseListener() {
            //                                        @Override
            //                                        public void windowClose( Window.CloseEvent closeEvent ) {
            //                                            refreshClustersInfo();
            //                                        }
            //                                    } );
            //                                    contentRoot.getUI().addWindow( window.getWindow() );
            //                                }
            //                            } );
            //
            //                            alert.getCancel().addClickListener( new Button.ClickListener() {
            //                                @Override
            //                                public void buttonClick( Button.ClickEvent clickEvent ) {
            //                                    UUID trackID = spark.changeMasterNode( config.getClusterName(),
            // agent.getHostname(),
            //                                            false );
            //                                    ProgressWindow window = new ProgressWindow( executor, tracker,
            // trackID,
            //                                            SparkClusterConfig.PRODUCT_KEY );
            //                                    window.getWindow().addCloseListener( new Window.CloseListener() {
            //                                        @Override
            //                                        public void windowClose( Window.CloseEvent closeEvent ) {
            //                                            refreshClustersInfo();
            //                                        }
            //                                    } );
            //                                    contentRoot.getUI().addWindow( window.getWindow() );
            //                                }
            //                            } );
            //
            //                            contentRoot.getUI().addWindow( alert.getAlert() );
            //                        }
            //                    } );
            //
            //                    contentRoot.getUI().addWindow( alert.getAlert() );
            //                }
            //            } );

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s node?", agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = spark.destroySlaveNode( config.getClusterName(), agent.getHostname() );
                            ProgressWindow window =
                                    new ProgressWindow( executor, tracker, trackID, SparkClusterConfig.PRODUCT_KEY );
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

        //add master here
        final Button checkBtn = new Button( "Check" );
        checkBtn.addStyleName( "default" );
        final Button startBtn = new Button( "Start" );
        startBtn.addStyleName( "default" );
        final Button stopBtn = new Button( "Stop" );
        stopBtn.addStyleName( "default" );
        final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
        stopBtn.setEnabled( false );
        startBtn.setEnabled( false );
        progressIcon.setVisible( false );

        table.addItem( new Object[] {
                MASTER_PREFIX + master.getHostname() + String.format( " (%s)", master.getListIP().get( 0 ) ), checkBtn,
                startBtn, stopBtn, null, null, progressIcon
        }, null );

        checkBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                executor.execute( new CheckTask( tracker, spark, config.getClusterName(), master.getHostname(), true,
                        new CompleteEvent()
                        {

                            @Override
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

                if ( !stopBtn.isEnabled() )
                {
                    Notification.show( "Node already started" );
                }

                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                executor.execute( new StartTask( tracker, spark, config.getClusterName(), master.getHostname(), true,
                        new CompleteEvent()
                        {

                            @Override
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

                if ( !startBtn.isEnabled() )
                {
                    Notification.show( "Node already stopped" );
                }

                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                executor.execute( new StopTask( tracker, spark, config.getClusterName(), master.getHostname(), true,
                        new CompleteEvent()
                        {

                            @Override
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
                                    progressIcon.setVisible( false );
                                }
                            }
                        } ) );
            }
        } );
    }


    public void refreshClustersInfo()
    {
        List<SparkClusterConfig> clustersInfo = spark.getClusters();
        SparkClusterConfig clusterInfo = ( SparkClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && clustersInfo.size() > 0 )
        {
            for ( SparkClusterConfig ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                clusterCombo.setItemCaption( ci, ci.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( SparkClusterConfig mongoClusterInfo : clustersInfo )
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
                clusterCombo.setValue( clustersInfo.iterator().next() );
            }
        }
    }


    public void startAllNodes()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Start" ).getValue() );
            checkBtn.click();
        }
    }


    public void stopAllNodes()
    {
        for ( Object o : nodesTable.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = nodesTable.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Stop" ).getValue() );
            checkBtn.click();
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
