package org.safehaus.subutai.plugin.hadoop.ui.manager;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.CheckTask;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.StartTask;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.StopTask;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager {

    private final static String CHECK_ALL_BUTTON_CAPTION="Check All";
    private final static String START_ALL_BUTTON_CAPTION="Start All";
    private final static String STOP_ALL_BUTTON_CAPTION="Stop All";
    private final static String DESTROY_CLUSTER_BUTTON_CAPTION="Destroy Cluster";
    private final static String ADD_NODE_BUTTON_CAPTION="Add Node";
    private final static String CHECK_BUTTON_CAPTION="Check";
    private final static String START_NAMENODE_BUTTON_CAPTION ="Start Namenode";
    private static final String START_JOBTRACKER_BUTTON_CAPTION = "Start JobTracker";
    private final static String STOP_NAMENODE_BUTTON_CAPTION ="Stop Namenode";
    private static final String STOP_JOBTRACKER_BUTTON_CAPTION = "Stop JobTracker";
    private final static String EXCLUDE_BUTTON_CAPTION="Exclude";
    private final static String INCLUDE_BUTTON_CAPTION="Include";
    private final static String DESTROY_BUTTON_CAPTION="Destroy";

    private final static String HOST_COLUMN_CAPTION="Host";
    private final static String NODE_ROLE_COLUMN_CAPTION="Node Role";
    private final static String STATUS_COLUMN_CAPTION="Status";
    private final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION="AVAILABLE_OPERATIONS";

    private final GridLayout contentRoot;
    private final ComboBox clusterList;
    private final Table masterNodesTable;
    private final Table slaveNodesTable;
    private final Label replicationFactor;
    private final Label domainName;
    private final Label slaveNodeCount;
    private Button checkAllButton;
    private HadoopClusterConfig hadoopCluster;
    private ProgressBar progressBar;
    private int processCount = 0;


    public Manager() {

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        masterNodesTable = createTableTemplate( "Master Nodes" );
        slaveNodesTable = createTableTemplate( "Slave Nodes" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterList = new ComboBox();
        clusterList.setImmediate( true );
        clusterList.setTextInputAllowed( false );
        clusterList.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterList.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                hadoopCluster = ( HadoopClusterConfig ) event.getProperty().getValue();
                refreshUI();
                checkNodesStatus( masterNodesTable );
                checkNodesStatus( slaveNodesTable );
            }
        } );

        controlsContent.addComponent( clusterList );

        checkAllButton = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllButton.addStyleName( "default" );
        checkAllButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                refreshClusterList();
                checkNodesStatus( masterNodesTable );
                checkNodesStatus( slaveNodesTable );
            }
        } );

        controlsContent.addComponent( checkAllButton );


        Button startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                startAllNodes( masterNodesTable );
            }
        } );
        controlsContent.addComponent( startAllBtn );

        Button stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                stopAllNodes( masterNodesTable );
            }
        } );
        controlsContent.addComponent( stopAllBtn );

        Button destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( hadoopCluster != null ) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", hadoopCluster.getClusterName() ),
                            "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            UUID trackID =
                                    HadoopUI.getHadoopManager().uninstallCluster( hadoopCluster.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                            HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    checkAllButton.click();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( destroyClusterBtn );

        Button addNodeButton = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeButton.addStyleName( "default" );
        addNodeButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( hadoopCluster != null ) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add slave node to the %s cluster?",
                                    hadoopCluster.getClusterName() ), "Yes", "No"
                    );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            UUID trackID = HadoopUI.getHadoopManager().addNode( hadoopCluster.getClusterName(), 1 );
                            ProgressWindow window =
                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                            HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    checkAllButton.click();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( addNodeButton );

        HorizontalLayout configContent = new HorizontalLayout();
        configContent.setSpacing( true );

        replicationFactor = new Label();
        domainName = new Label();
        slaveNodeCount = new Label();
        progressBar = new ProgressBar();
        progressBar.setIndeterminate( true );
        progressBar.setVisible( false );
        configContent.addComponent( new Label( "Replication Factor:" ) );
        configContent.addComponent( replicationFactor );
        configContent.addComponent( new Label( "Domain:" ) );
        configContent.addComponent( domainName );
        configContent.addComponent( new Label( "Slave Node Count:" ) );
        configContent.addComponent( slaveNodeCount );
        configContent.addComponent( progressBar );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( configContent, 0, 1 );
        contentRoot.addComponent( masterNodesTable, 0, 2, 0, 3 );
        contentRoot.addComponent( slaveNodesTable, 0, 4, 0, 9 );

        checkAllButton.click();
    }


    private Table createTableTemplate( String caption ) {
        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( NODE_ROLE_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( STATUS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( getTableClickListener( table ) );

        return table;
    }


    private void refreshUI() {
        if ( hadoopCluster != null ) {
            populateTable( masterNodesTable, getMasterNodeList( hadoopCluster ) );
            populateTable( slaveNodesTable, hadoopCluster.getDataNodes() );
            replicationFactor.setValue( hadoopCluster.getReplicationFactor().toString() );
            domainName.setValue( hadoopCluster.getDomainName() );
            slaveNodeCount.setValue( hadoopCluster.getAllSlaveNodes().size()+"" );
        }
        else {
            masterNodesTable.removeAllItems();
            slaveNodesTable.removeAllItems();
            replicationFactor.setValue( "" );
            domainName.setValue( "" );
            slaveNodeCount.setValue( "" );
        }
    }


    public void refreshClusterList() {
        List<HadoopClusterConfig> hadoopClusterList = HadoopUI.getHadoopManager().getClusters();
        HadoopClusterConfig clusterInfo = ( HadoopClusterConfig ) clusterList.getValue();
        clusterList.removeAllItems();
        if ( hadoopClusterList != null && hadoopClusterList.size() > 0 ) {
            for ( HadoopClusterConfig hadoopCluster : hadoopClusterList ) {
                clusterList.addItem( hadoopCluster );
                clusterList.setItemCaption( hadoopCluster, hadoopCluster.getClusterName() );
            }
            if ( clusterInfo != null ) {
                for ( HadoopClusterConfig hadoopCluster : hadoopClusterList ) {
                    if ( hadoopCluster.getClusterName().equals( clusterInfo.getClusterName() ) ) {
                        clusterList.setValue( hadoopCluster );
                        return;
                    }
                }
            }
            else {
                clusterList.setValue( hadoopClusterList.iterator().next() );
            }
        }
    }


    public static void checkNodesStatus( Table table ) {
        if ( table != null ) {
            for ( Object o : table.getItemIds() ) {
                int rowId = ( Integer ) o;
                HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( table, rowId );
                if ( availableOperationsLayout != null ) {
                    Button checkButton = getCheckButton( availableOperationsLayout );
                    if ( checkButton != null )
                        checkButton.click();
                }
            }
        }

    }


    public static void startAllNodes( Table table ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( table, rowId );
            if ( availableOperationsLayout != null ) {
                Button startButton = getStartButton( availableOperationsLayout );
                if ( startButton != null )
                    startButton.click();
            }
        }
    }


    public static void stopAllNodes( Table table ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( table, rowId );
            if ( availableOperationsLayout != null ) {
                Button startButton = getStopButton( availableOperationsLayout );
                if ( startButton != null )
                    startButton.click();
            }
        }
    }


    private static HorizontalLayout getAvailableOperationsLayout( Table table, int rowId) {
        Item row = table.getItem( rowId );
        HorizontalLayout buttonLayout = ( HorizontalLayout ) ( row.getItemProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION ).getValue() );
        return buttonLayout;
    }


    private static Button getCheckButton( final HorizontalLayout availableOperationsLayout ) {
        if ( availableOperationsLayout == null )
            return null;
        else
        {
            for ( Component component : availableOperationsLayout ) {
                if ( component.getCaption().equals( CHECK_BUTTON_CAPTION ) )
                    return ( Button ) component;
            }
            // If not found
            return null;
        }

    }


    private static Button getStartButton( final HorizontalLayout availableOperationsLayout ) {
        if ( availableOperationsLayout == null )
            return null;
        else
        {
            for ( Component component : availableOperationsLayout ) {
                if ( component.getCaption().equals( START_NAMENODE_BUTTON_CAPTION )
                        || component.getCaption().equals( START_JOBTRACKER_BUTTON_CAPTION ) )
                    return ( Button ) component;
            }
            // If not found
            return null;
        }
    }


    private static Button getStopButton( final HorizontalLayout availableOperationsLayout ) {
        if ( availableOperationsLayout == null )
            return null;
        else
        {
            for ( Component component : availableOperationsLayout ) {
                if ( component.getCaption().equals( STOP_NAMENODE_BUTTON_CAPTION )
                        || component.getCaption().equals( STOP_JOBTRACKER_BUTTON_CAPTION ) )
                    return ( Button ) component;
            }
            // If not found
            return null;
        }
    }


    private void show( String notification ) {
        Notification.show( notification );
    }


    private void populateTable( final Table table, List<Agent> agents ) {

        table.removeAllItems();

        for ( final Agent agent : agents ) {

            // Layouts to be added to table
            final HorizontalLayout availableOperations = new HorizontalLayout();
            final HorizontalLayout statusGroup = new HorizontalLayout();

            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );
            statusGroup.addStyleName( "default" );
            statusGroup.setSpacing( true );
            // Layouts to be added to table

            // Buttons to be added to availableOperations
            final Button checkButton = new Button( CHECK_BUTTON_CAPTION );
            final Button destroyButton = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
            final Button startStopButton = new Button("Start/Stop");
            final Button excludeIncludeNodeButton = new Button("Exclude/Include");

            checkButton.addStyleName( "default" );
            startStopButton.addStyleName( "default" );
            startStopButton.setEnabled( false );
            excludeIncludeNodeButton.addStyleName( "default" );
            excludeIncludeNodeButton.setEnabled( false );
            destroyButton.addStyleName( "default" );
            // Buttons to be added to availableOperations


            // Labels to be added to statusGroup
            final Label statusDatanode = new Label("");
            final Label statusTaskTracker = new Label("");

            statusDatanode.addStyleName( "default" );
            statusTaskTracker.addStyleName( "default" );
            // Labels to be added to statusGroup


            // Populate table with proper content according to node types
            startStopButton.setEnabled( false );
            final HadoopClusterConfig cluster = HadoopUI.getHadoopManager().getCluster( hadoopCluster.getClusterName() );


            // If master node
            if ( cluster.isMasterNode( agent ) ) {
                // If Namenode
                if ( cluster.isNameNode( agent ) ) {

                    checkButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            checkButton.setEnabled( false );
                            startStopButton.setEnabled( false );
                            HadoopUI.getExecutor()
                                    .execute( new CheckTask( NodeType.NAMENODE, hadoopCluster, new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            if ( state == NodeState.RUNNING ) {
                                                statusDatanode.setValue( "Namenode Running" );
                                                startStopButton.setCaption( STOP_NAMENODE_BUTTON_CAPTION );
                                                startStopButton.setEnabled( true );
                                            }
                                            else if ( state == NodeState.STOPPED ) {
                                                statusDatanode.setValue( "Namenode Stopped" );
                                                startStopButton.setCaption( START_NAMENODE_BUTTON_CAPTION );
                                                startStopButton.setEnabled( true );
                                            }
                                            else {
                                                statusDatanode.setValue( "Namenode Not Connected" );
                                                startStopButton.setCaption( "Not connected" );
                                            }
                                            checkButton.setEnabled( true );
                                        }
                                    }, null, agent ) );
                        }
                    } );

                    startStopButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            startStopButton.setEnabled( false );
                            enableProgressBar();
                            boolean isRunning = startStopButton.getCaption().equals( STOP_NAMENODE_BUTTON_CAPTION );
                            if ( ! isRunning ) {
                                HadoopUI.getExecutor().execute(

                                        new StartTask( NodeType.NAMENODE, hadoopCluster, new CompleteEvent() {

                                            public void onComplete( NodeState state ) {
                                                try {
                                                    Thread.sleep( 1000 );
                                                }
                                                catch ( InterruptedException e ) {
                                                    show( "Exception: " + e );
                                                }
                                                disableProgressBar();
                                                checkAllIfNoProcessRunning();
                                                startStopButton.setEnabled( true );
                                            }
                                        }, null, agent )
                                                              );
                            }
                            else {
                                HadoopUI.getExecutor().execute(

                                        new StopTask( NodeType.NAMENODE, hadoopCluster, new CompleteEvent() {

                                            public void onComplete( NodeState state ) {

                                                disableProgressBar();
                                                checkAllIfNoProcessRunning();
                                                startStopButton.setEnabled( true );
                                            }
                                        }, null, agent )
                                                              );
                            }

                        }
                    } );

                }
                // If Jobtracker
                else if ( cluster.isJobTracker( agent ) ) {

                    checkButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            startStopButton.setEnabled( false );
                            checkButton.setEnabled( false );
                            HadoopUI.getExecutor().execute(
                                    new CheckTask( NodeType.JOBTRACKER, hadoopCluster, new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            if ( state == NodeState.RUNNING ) {
                                                statusTaskTracker.setValue( "JobTracker Running" );
                                                startStopButton.setCaption( STOP_JOBTRACKER_BUTTON_CAPTION );
                                                startStopButton.setEnabled( true );
                                            }
                                            else if ( state == NodeState.STOPPED ) {
                                                statusTaskTracker.setValue( "JobTracker Stopped" );
                                                startStopButton.setCaption( START_JOBTRACKER_BUTTON_CAPTION );
                                                startStopButton.setEnabled( true );
                                            }
                                            else {
                                                statusTaskTracker.setValue( "JobTracker Not Connected" );
                                                startStopButton.setCaption( "Not connected" );
                                            }

                                            checkButton.setEnabled( true );
                                        }
                                    }, null, agent )
                                                          );
                        }
                    } );

                    startStopButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            startStopButton.setEnabled( false );
                            enableProgressBar();
                            boolean isRunning = startStopButton.getCaption().equals( STOP_JOBTRACKER_BUTTON_CAPTION );
                            if ( ! isRunning ) {
                                startStopButton.setEnabled( false );
                                HadoopUI.getExecutor().execute(
                                        new StartTask( NodeType.JOBTRACKER, hadoopCluster, new CompleteEvent() {

                                            public void onComplete( NodeState state ) {
                                                try {
                                                    Thread.sleep( 1000 );
                                                }
                                                catch ( InterruptedException e ) {
                                                    show( "Exception: " + e );
                                                }
                                                disableProgressBar();
                                                checkAllIfNoProcessRunning();
                                                startStopButton.setEnabled( true );
                                            }
                                        }, null, agent )
                                                              );
                            }
                            else {
                                startStopButton.setEnabled( false );
                                HadoopUI.getExecutor().execute(
                                        new StopTask( NodeType.JOBTRACKER, hadoopCluster, new CompleteEvent() {

                                            public void onComplete( NodeState state ) {
                                                disableProgressBar();
                                                checkAllIfNoProcessRunning();
                                                startStopButton.setEnabled( true );
                                            }
                                        }, null, agent )
                                                              );
                            }
                        }
                    } );

                }
                else if ( cluster.isSecondaryNameNode( agent ) ) {
                    checkButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            checkButton.setEnabled( false );
                            HadoopUI.getExecutor().execute(
                                    new CheckTask( NodeType.SECONDARY_NAMENODE, hadoopCluster, new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            if ( state == NodeState.RUNNING ) {
                                                statusDatanode.setValue( "SecondaryNameNode Running" );
                                            }
                                            else if ( state == NodeState.STOPPED ) {
                                                statusDatanode.setValue( "SecondaryNameNode Stopped" );
                                            }
                                            else {
                                                statusDatanode.setValue( "SecondaryNameNode Not Connected" );
                                            }

                                            checkButton.setEnabled( true );
                                        }
                                    }, null, agent )
                                                          );
                        }
                    } );
                }
            }
            // If slave node
            else {
                statusGroup.addComponent( statusDatanode );
                statusGroup.addComponent( statusTaskTracker );

                if ( cluster.getBlockedAgents().contains( agent ) ) {
                    excludeIncludeNodeButton.setCaption( INCLUDE_BUTTON_CAPTION );
                    if ( cluster.getDecommissioningNodes().contains( agent ) ) {
                        destroyButton.setCaption( "Decommissioning" );
                    }
                    else {
                        destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                    }
                }
                else {
                    excludeIncludeNodeButton.setCaption( EXCLUDE_BUTTON_CAPTION );
                    destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                }

                checkButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {
                        checkButton.setEnabled( false );
                        excludeIncludeNodeButton.setEnabled( false );
                        destroyButton.setEnabled( false );
                        if ( HadoopUI.getHadoopManager().getCluster( hadoopCluster.getClusterName() )
                                     .isDataNode( agent ) ) {
                            HadoopUI.getExecutor()
                                    .execute( new CheckTask( NodeType.DATANODE, hadoopCluster, new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            if ( state == NodeState.RUNNING ) {
                                                statusDatanode.setValue( "Datanode Running" );
                                            }
                                            else if ( state == NodeState.STOPPED ) {
                                                statusDatanode.setValue( "Datanode Stopped" );
                                            }
                                            else {
                                                statusDatanode.setValue( "Not connected" );
                                            }

                                            if ( cluster.getBlockedAgents().contains( agent ) ) {
                                                excludeIncludeNodeButton.setCaption( INCLUDE_BUTTON_CAPTION );
                                                if ( cluster.getDecommissioningNodes().contains( agent ) ) {
                                                    destroyButton.setCaption( "Decommissioning" );
                                                }
                                                else {
                                                    destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                                                }
                                            }
                                            else {
                                                excludeIncludeNodeButton.setCaption( EXCLUDE_BUTTON_CAPTION );
                                                destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                                            }

                                            excludeIncludeNodeButton.setEnabled( true );
                                            checkButton.setEnabled( true );
                                            destroyButton.setEnabled( true );
                                        }
                                    }, null, agent ) );
                        }
                        if ( HadoopUI.getHadoopManager().getCluster( hadoopCluster.getClusterName() )
                                     .isTaskTracker( agent ) ) {
                            HadoopUI.getExecutor()
                                    .execute( new CheckTask( NodeType.TASKTRACKER, hadoopCluster, new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            if ( state == NodeState.RUNNING ) {
                                                statusTaskTracker.setValue( "Tasktracker Running" );
                                            }
                                            else if ( state == NodeState.STOPPED ) {
                                                statusTaskTracker.setValue( "Tasktracker Stopped" );
                                            }
                                            else {
                                                statusTaskTracker.setValue( "Not connected" );
                                            }

                                            if ( cluster.getBlockedAgents().contains( agent ) ) {
                                                excludeIncludeNodeButton.setCaption( INCLUDE_BUTTON_CAPTION );
                                                if ( cluster.getDecommissioningNodes().contains( agent ) ) {
                                                    destroyButton.setCaption( "Decommissioning" );
                                                }
                                                else {
                                                    destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                                                }
                                            }
                                            else {
                                                excludeIncludeNodeButton.setCaption( EXCLUDE_BUTTON_CAPTION );
                                                destroyButton.setCaption( DESTROY_BUTTON_CAPTION );
                                            }
                                            excludeIncludeNodeButton.setEnabled( true );
                                            checkButton.setEnabled( true );
                                        }
                                    }, null, agent ) );
                        }
                    }
                } );

                excludeIncludeNodeButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {

                        if ( cluster.getBlockedAgents().contains( agent ) ) {
                            ConfirmationDialog alert = new ConfirmationDialog(
                                    String.format( "Do you want to include the %s node?", agent.getHostname() ), "Yes",
                                    "No" );
                            alert.getOk().addClickListener( new Button.ClickListener() {
                                @Override
                                public void buttonClick( Button.ClickEvent clickEvent ) {
                                    checkButton.setEnabled( false );
                                    excludeIncludeNodeButton.setEnabled( false );
                                    UUID trackID = HadoopUI.getHadoopManager().includeNode( hadoopCluster, agent );
                                    ProgressWindow window =
                                            new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                                    HadoopClusterConfig.PRODUCT_KEY );
                                    window.getWindow().addCloseListener( new Window.CloseListener() {
                                        @Override
                                        public void windowClose( Window.CloseEvent closeEvent ) {
                                            checkButton.click();
                                            checkButton.setEnabled( true );
                                            excludeIncludeNodeButton.setEnabled( true );
                                        }
                                    } );
                                    contentRoot.getUI().addWindow( window.getWindow() );
                                }
                            } );

                            contentRoot.getUI().addWindow( alert.getAlert() );
                        }
                        else {
                            ConfirmationDialog alert = new ConfirmationDialog(
                                    String.format( "Do you want to exclude the %s node?", agent.getHostname() ), "Yes",
                                    "No" );
                            alert.getOk().addClickListener( new Button.ClickListener() {
                                @Override
                                public void buttonClick( Button.ClickEvent clickEvent ) {
                                    checkButton.setEnabled( false );
                                                            excludeIncludeNodeButton.setEnabled( false );
                                    UUID trackID = HadoopUI.getHadoopManager().excludeNode( hadoopCluster, agent );
                                    ProgressWindow window =
                                            new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                                    HadoopClusterConfig.PRODUCT_KEY );
                                    window.getWindow().addCloseListener( new Window.CloseListener() {
                                        @Override
                                        public void windowClose( Window.CloseEvent closeEvent ) {
                                            checkButton.click();
                                            checkButton.setEnabled( true );
                                            excludeIncludeNodeButton.setEnabled( true );
                                        }
                                    } );
                                    contentRoot.getUI().addWindow( window.getWindow() );
                                }
                            } );

                            contentRoot.getUI().addWindow( alert.getAlert() );

                        }

                    }
                } );


                destroyButton.addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {
                        String question;
                        if ( cluster.getDecommissioningNodes().contains( agent ) ) {
                            question = "Do you want to destroy the decommissioning %s node?";
                        }
                        else {
                            question = "Do you want to destroy the %s node?";
                        }

                        ConfirmationDialog alert =
                                new ConfirmationDialog( String.format( question, agent.getHostname() ), "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener() {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent ) {
                                UUID trackID = HadoopUI.getHadoopManager().destroyNode( hadoopCluster, agent );
                                ProgressWindow window =
                                        new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                                HadoopClusterConfig.PRODUCT_KEY );
                                window.getWindow().addCloseListener( new Window.CloseListener() {
                                    @Override
                                    public void windowClose( Window.CloseEvent closeEvent ) {
                                        refreshUI();
                                    }
                                } );
                                contentRoot.getUI().addWindow( window.getWindow() );
                            }
                        } );

                        contentRoot.getUI().addWindow( alert.getAlert() );
                    }
                } );
            }

            // Add UI components into relevant fields according to its role in cluster
            if ( cluster.isMasterNode( agent ) ) {
                if ( cluster.isNameNode( agent )  ) {
                    availableOperations.addComponent( checkButton );
                    availableOperations.addComponent( startStopButton );
                    statusGroup.addComponent( statusDatanode );
                }
                else if ( cluster.isJobTracker( agent ) ) {
                    availableOperations.addComponent( checkButton );
                    availableOperations.addComponent( startStopButton );
                    statusGroup.addComponent( statusTaskTracker );
                }
                else  if ( cluster.isSecondaryNameNode( agent ) ) {
                    availableOperations.addComponent( checkButton );
                    statusGroup.addComponent( statusDatanode );
                }
            }
            else {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( excludeIncludeNodeButton );
                availableOperations.addComponent( destroyButton );
                statusGroup.addComponent( statusDatanode );
                statusGroup.addComponent( statusTaskTracker );
            }
            table.addItem( new Object[] {
                    agent.getHostname(), cluster.getNodeRoles( agent ).toString(),
                    statusGroup, availableOperations
            }, null );
        }
    }


    private void enableProgressBar() {
        processCount++;
        progressBar.setVisible( true );
    }


    private void disableProgressBar() {
        if ( processCount > 0 ) {
            processCount--;
        }
        if ( processCount == 0 ) {
            progressBar.setVisible( false );
        }
    }


    private void checkAllIfNoProcessRunning() {
        if ( processCount == 0 ) {
            checkAllButton.click();
        }
    }

    public Component getContent() {
        return contentRoot;
    }


    public List<Agent> getMasterNodeList( final HadoopClusterConfig hadoopCluster ) {
        List<Agent> masterNodeList = new ArrayList<>();
        masterNodeList.add( hadoopCluster.getNameNode() );
        masterNodeList.add( hadoopCluster.getJobTracker() );
        masterNodeList.add( hadoopCluster.getSecondaryNameNode() );
        return masterNodeList;
    }


    public ItemClickEvent.ItemClickListener getTableClickListener( final Table table ) {
        return new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick( ItemClickEvent event ) {
                if ( event.isDoubleClick() ) {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = HadoopUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), HadoopUI.getExecutor(),
                                        HadoopUI.getCommandRunner(), HadoopUI.getAgentManager() );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else {
                        show( "Agent is not connected for" + lxcHostname );
                    }
                }
            }
        } ;
    }
}
