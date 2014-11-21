package org.safehaus.subutai.plugin.hadoop.ui.manager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
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


public class Manager
{

    public final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    public final static String START_ALL_BUTTON_CAPTION = "Start All";
    public final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    public final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    public final static String ADD_NODE_BUTTON_CAPTION = "Add Node";
    public final static String CHECK_BUTTON_CAPTION = "Check";
    public final static String START_BUTTON_CAPTION = "Start";
    public final static String STOP_BUTTON_CAPTION = "Stop";
    public final static String DESTROY_BUTTON_CAPTION = "Destroy";
    public final static String HOST_COLUMN_CAPTION = "Host";
    public final static String IP_COLUMN_CAPTION = "IP List";
    public final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    public final static String STATUS_COLUMN_CAPTION = "Status";
    public final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    public final static String START_STOP_BUTTON_DEFAULT_CAPTION = "Start/Stop";
    protected final static String START_NAMENODE_BUTTON_CAPTION = "Start Namenode";
    protected final static String START_JOBTRACKER_BUTTON_CAPTION = "Start JobTracker";
    protected final static String STOP_NAMENODE_BUTTON_CAPTION = "Stop Namenode";
    protected final static String STOP_JOBTRACKER_BUTTON_CAPTION = "Stop JobTracker";
    protected final static String EXCLUDE_BUTTON_CAPTION = "Exclude";
    protected final static String INCLUDE_BUTTON_CAPTION = "Include";
    protected final static String URL_BUTTON_CAPTION = "URL";
    protected final static String DECOMMISSION_STATUS_CAPTION = "Decommission Status: ";
    protected final static String EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION = "Exclude/Include";
    private final ComboBox clusterList;
    private final Table masterNodesTable;
    private final Table slaveNodesTable;
    private final Label replicationFactor;
    private final Label domainName;
    private final Label slaveNodeCount;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final EnvironmentManager environmentManager;
    protected GridLayout contentRoot;
    protected ProgressBar progressBar;
    protected int processCount = 0;
    private Button checkAllButton;
    private HadoopClusterConfig hadoopCluster;
    private String decommissionStatus;
    private ManagerListener managerListener;


    public Manager( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        super();
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.environmentManager = serviceLocator.getService( EnvironmentManager.class );

        managerListener = new ManagerListener( this );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 40 );
        contentRoot.setColumns( 1 );

        progressBar = new ProgressBar();
        progressBar.setId( "indicator" );
        progressBar.setIndeterminate( true );
        progressBar.setVisible( false );

        //tables go here
        masterNodesTable = createTableTemplate( "Master Nodes" );
        masterNodesTable.setId( "HadoopMasterNodesTable" );
        slaveNodesTable = createTableTemplate( "Slave Nodes" );
        slaveNodesTable.setId( "HadoopSlaveNodesTable" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterList = new ComboBox();
        clusterList.setId( "HadoopClustersCb" );
        clusterList.setImmediate( true );
        clusterList.setTextInputAllowed( false );
        clusterList.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterList.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                hadoopCluster = ( HadoopClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterList );

        checkAllButton = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllButton.setId( "HadoopCheckAll" );
        checkAllButton.addStyleName( "default" );
        checkAllButton.addClickListener( managerListener.checkAllButtonListener( checkAllButton ) );

        controlsContent.addComponent( checkAllButton );


        Button startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.setId( "HadoopStartAll" );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                startAllNodes( masterNodesTable );
            }
        } );
        controlsContent.addComponent( startAllBtn );

        Button stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.setId( "HadoopStopAll" );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                stopAllNodes( masterNodesTable );
            }
        } );
        controlsContent.addComponent( stopAllBtn );

        Button destroyClusterButton = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterButton.setId( "HadoopDestroyCluster" );
        destroyClusterButton.addStyleName( "default" );
        destroyClusterButton.addClickListener( managerListener.destroyClusterButtonListener() );

        controlsContent.addComponent( destroyClusterButton );

        Button addNodeButton = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeButton.setId( "HadoopaddNode" );
        addNodeButton.addStyleName( "default" );
        addNodeButton.addClickListener( managerListener.addNodeButtonListener() );

        controlsContent.addComponent( addNodeButton );

        HorizontalLayout configContent = new HorizontalLayout();
        configContent.setSpacing( true );

        replicationFactor = new Label();
        domainName = new Label();
        slaveNodeCount = new Label();
        configContent.addComponent( new Label( "Replication Factor:" ) );
        configContent.addComponent( replicationFactor );
        configContent.addComponent( new Label( "Domain:" ) );
        configContent.addComponent( domainName );
        configContent.addComponent( new Label( "Slave Node Count:" ) );
        configContent.addComponent( slaveNodeCount );
        configContent.addComponent( getProgressBar() );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( configContent, 0, 1 );
        contentRoot.addComponent( masterNodesTable, 0, 2, 0, 10 );
        contentRoot.addComponent( slaveNodesTable, 0, 11, 0, 38 );

        checkAllButton.click();
    }


    public ProgressBar getProgressBar()
    {
        return progressBar;
    }


    public Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( IP_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( NODE_ROLE_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( STATUS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setColumnExpandRatio( HOST_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( IP_COLUMN_CAPTION, 0.1f );
        table.setColumnExpandRatio( NODE_ROLE_COLUMN_CAPTION, 0.15f );
        table.setColumnExpandRatio( STATUS_COLUMN_CAPTION, 0.35f );
        table.setColumnExpandRatio( AVAILABLE_OPERATIONS_COLUMN_CAPTION, 0.30f );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( managerListener.getTableClickListener( table ) );

        return table;
    }


    private void refreshUI()
    {
        if ( hadoopCluster != null )
        {
            Environment environment = environmentManager.getEnvironmentByUUID( hadoopCluster.getEnvironmentId() );

            populateTable( masterNodesTable, getMasters( environment.getContainers(), hadoopCluster ) );
            populateTable( slaveNodesTable, getSlaves( environment.getContainers(), hadoopCluster ) );

            replicationFactor.setValue( hadoopCluster.getReplicationFactor().toString() );
            domainName.setValue( hadoopCluster.getDomainName() );
            slaveNodeCount.setValue( hadoopCluster.getAllSlaveNodes().size() + "" );
        }
        else
        {
            masterNodesTable.removeAllItems();
            slaveNodesTable.removeAllItems();
            replicationFactor.setValue( "" );
            domainName.setValue( "" );
            slaveNodeCount.setValue( "" );
        }
    }


    protected void populateTable( final Table table, Set<ContainerHost> containerHosts )
    {
        table.removeAllItems();
        // Add UI components into relevant fields according to its role in cluster
        for ( final ContainerHost containerHost : containerHosts )
        {
            addRowComponents( table, containerHost );
        }
    }


    public void addRowComponents( Table table, final ContainerHost containerHost )
    {

        final HadoopClusterConfig cluster = hadoop.getCluster( hadoopCluster.getClusterName() );

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
        checkButton.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopCheck" );
        final Button destroyButton = new Button( DESTROY_BUTTON_CAPTION );
        destroyButton.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopDestroy" );
        final Button startStopButton = new Button( START_STOP_BUTTON_DEFAULT_CAPTION );
        startStopButton.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopStartStop" );
        final Button excludeIncludeNodeButton = new Button( EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION );
        excludeIncludeNodeButton.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopExcludeInclude" );
        final Button urlButton = new Button( URL_BUTTON_CAPTION );
        urlButton.setId( containerHost.getHostname() + "-hadoopUrl" );

        checkButton.addStyleName( "default" );
        startStopButton.addStyleName( "default" );
        excludeIncludeNodeButton.addStyleName( "default" );
        destroyButton.addStyleName( "default" );
        urlButton.addStyleName( "default" );
        // Buttons to be added to availableOperations


        // Labels to be added to statusGroup
        final Label statusDatanode = new Label( "" );
        statusDatanode.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopStatusDataNode" );
        final Label statusTaskTracker = new Label( "" );
        statusTaskTracker.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopStatusTaskTracker" );
        final Label statusDecommission = new Label( "" );
        statusDecommission.setId( containerHost.getAgent().getListIP().get( 0 ) + "-hadoopStatusDecommission" );

        statusDatanode.addStyleName( "default" );
        statusTaskTracker.addStyleName( "default" );
        statusDecommission.addStyleName( "default" );
        // Labels to be added to statusGroup


        if ( cluster.isMasterNode( containerHost ) )
        {
            if ( cluster.isNameNode( containerHost.getAgent().getUuid() ) )
            {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( startStopButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusDatanode );
            }
            else if ( cluster.isJobTracker( containerHost.getAgent().getUuid() ) )
            {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( startStopButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusTaskTracker );
            }
            else if ( cluster.isSecondaryNameNode( containerHost.getAgent().getUuid() ) )
            {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusDatanode );
            }
        }
        else
        {
            availableOperations.addComponent( checkButton );
            availableOperations.addComponent( excludeIncludeNodeButton );
            availableOperations.addComponent( destroyButton );
            statusGroup.addComponent( statusDatanode );
            statusGroup.addComponent( statusTaskTracker );
            statusGroup.addComponent( statusDecommission );
        }
        table.addItem( new Object[] {
                containerHost.getHostname(), containerHost.getAgent().getListIP().get( 0 ),
                getNodeRoles( cluster, containerHost ).toString(), statusGroup, availableOperations
        }, null );


        Item row = getAgentRow( table, containerHost.getAgent() );

        // Add listeners according to node type

        // If master node
        if ( cluster.isMasterNode( containerHost ) )
        {
            // If Namenode
            if ( cluster.isNameNode( containerHost.getAgent().getUuid() ) )
            {
                urlButton.addClickListener( managerListener.nameNodeURLButtonListener( containerHost.getAgent() ) );
                checkButton.addClickListener( managerListener.nameNodeCheckButtonListener( row ) );
                startStopButton.addClickListener( managerListener.nameNodeStartStopButtonListener( row ) );
            }
            // If Jobtracker
            else if ( cluster.isJobTracker( containerHost.getAgent().getUuid() ) )
            {
                urlButton.addClickListener( jobTrackerURLButtonListener( containerHost.getAgent() ) );
                checkButton.addClickListener( managerListener.jobTrackerCheckButtonListener( row ) );
                startStopButton.addClickListener( managerListener.jobTrackerStartStopButtonListener( row ) );
            }
            // If SecondaryNameNode
            else if ( cluster.isSecondaryNameNode( containerHost.getAgent().getUuid() ) )
            {
                urlButton.addClickListener(
                        managerListener.secondaryNameNodeURLButtonListener( containerHost.getAgent() ) );
                checkButton.addClickListener( managerListener.secondaryNameNodeCheckButtonListener( row ) );
            }
        }
        // If slave node
        else
        {
            checkButton.addClickListener( managerListener.slaveNodeCheckButtonListener( row ) );
            excludeIncludeNodeButton.addClickListener( managerListener.slaveNodeExcludeIncludeButtonListener( row ) );
            destroyButton.addClickListener( managerListener.slaveNodeDestroyButtonListener( row ) );
        }
    }


    public Item getAgentRow( final Table table, final Agent agent )
    {

        int rowId = getAgentRowId( table, agent );
        Item row = null;
        if ( rowId >= 0 )
        {
            row = table.getItem( rowId );
        }
        if ( row == null )
        {
            Notification.show( "Agent rowId should have been found inside " + table.getCaption()
                    + " but could not find! " );
        }
        return row;
    }


    protected int getAgentRowId( final Table table, final Agent agent )
    {
        if ( table != null && agent != null )
        {
            for ( Object o : table.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = table.getItem( rowId );
                String hostName = row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();
                if ( hostName.equals( agent.getHostname() ) )
                {
                    return rowId;
                }
            }
        }
        return -1;
    }


    private List<NodeType> getNodeRoles( HadoopClusterConfig clusterConfig, final ContainerHost containerHost )
    {
        List<NodeType> nodeRoles = new ArrayList<>();

        if ( clusterConfig.isNameNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.NAMENODE );
        }
        if ( clusterConfig.isSecondaryNameNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.SECONDARY_NAMENODE );
        }
        if ( clusterConfig.isJobTracker( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.JOBTRACKER );
        }
        if ( clusterConfig.isDataNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.DATANODE );
        }
        if ( clusterConfig.isTaskTracker( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.TASKTRACKER );
        }

        return nodeRoles;
    }


    private Button.ClickListener jobTrackerURLButtonListener( final Agent agent )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                contentRoot.getUI().getPage()
                           .open( "http://" + agent.getListIP().get( 0 ) + ":50030", "JobTracker", false );
            }
        };
    }


    public Set<ContainerHost> getMasters( Set<ContainerHost> containerHosts, HadoopClusterConfig config )
    {
        Set<ContainerHost> list = new HashSet<>();
        for ( ContainerHost containerHost : containerHosts )
        {
            if ( config.getAllMasterNodesAgents().contains( containerHost.getAgent().getUuid() ) )
            {
                list.add( containerHost );
            }
        }
        return list;
    }


    private Set<ContainerHost> getSlaves( Set<ContainerHost> containerHosts, HadoopClusterConfig config )
    {
        Set<ContainerHost> list = new HashSet<>();
        for ( ContainerHost containerHost : containerHosts )
        {
            if ( config.getAllSlaveNodesAgents().contains( containerHost.getAgent().getUuid() ) )
            {
                list.add( containerHost );
            }
        }
        return list;
    }


    private void startAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
            if ( availableOperationsLayout != null )
            {
                Button startButton = getStartButton( availableOperationsLayout );
                if ( startButton != null )
                {
                    startButton.click();
                }
            }
        }
    }


    public HorizontalLayout getAvailableOperationsLayout( Item row )
    {
        if ( row == null )
        {
            return null;
        }
        return ( HorizontalLayout ) ( row.getItemProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION ).getValue() );
    }


    public Button getStartButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( START_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    private void stopAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
            if ( availableOperationsLayout != null )
            {
                Button stopButton = getStopButton( availableOperationsLayout );
                if ( stopButton != null )
                {
                    stopButton.click();
                }
            }
        }
    }


    public Button getStopButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( STOP_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public HorizontalLayout getStatusLayout( final Item row )
    {
        if ( row == null )
        {
            return null;
        }
        return ( HorizontalLayout ) row.getItemProperty( STATUS_COLUMN_CAPTION ).getValue();
    }


    public Button getDestroyButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( DESTROY_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public Button getCheckButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( CHECK_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public synchronized void enableProgressBar()
    {
        incrementProcessCount();
        progressBar.setVisible( true );
    }


    public synchronized void incrementProcessCount()
    {
        processCount++;
    }


    public synchronized void disableProgressBar()
    {
        if ( processCount > 0 )
        {
            decrementProcessCount();
        }
        if ( processCount == 0 )
        {
            progressBar.setVisible( false );
        }
    }


    public synchronized void decrementProcessCount()
    {
        processCount--;
    }


    public Button getStartStopButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( START_BUTTON_CAPTION ) || component.getCaption().contains(
                        STOP_BUTTON_CAPTION ) || component.getCaption().equals( START_STOP_BUTTON_DEFAULT_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    protected Button getExcludeIncludeButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( EXCLUDE_BUTTON_CAPTION ) || component.getCaption().equals(
                        INCLUDE_BUTTON_CAPTION ) || component.getCaption()
                                                             .equals( EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            // If not found
            return null;
        }
    }


    protected void show( String notification )
    {
        Notification.show( notification );
    }


    protected Agent getAgentByRow( final Item row )
    {
        if ( row == null )
        {
            return null;
        }
        Environment environment = environmentManager.getEnvironmentByUUID( hadoopCluster.getEnvironmentId() );
        String lxcHostname = row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();

        for ( ContainerHost containerHost : environment.getContainers() )
        {
            if ( containerHost.getHostname().equals( lxcHostname ) )
            {
                return containerHost.getAgent();
            }
        }
        return null;
    }


    protected Label getStatusDatanodeLabel( final HorizontalLayout statusGroupLayout )
    {
        if ( statusGroupLayout == null )
        {
            return null;
        }
        return ( Label ) statusGroupLayout.getComponent( 0 );
    }


    protected Label getStatusTaskTrackerLabel( final HorizontalLayout statusGroupLayout )
    {
        if ( statusGroupLayout == null )
        {
            return null;
        }
        return ( Label ) statusGroupLayout.getComponent( 1 );
    }


    protected Label getStatusDecommissionLabel( final HorizontalLayout statusGroupLayout )
    {
        if ( statusGroupLayout == null )
        {
            return null;
        }
        return ( Label ) statusGroupLayout.getComponent( 2 );
    }


    protected NodeState getDecommissionStatus( final String operationLog, final Agent agent )
    {
        NodeState decommissionState = NodeState.UNKNOWN;
        String ipOfNode = agent.getListIP().get( 0 );

        if ( operationLog != null && operationLog.contains( ipOfNode ) )
        {
            String[] array = operationLog.split( "\n" );

            for ( int i = 0; i < array.length; i++ )
            {
                String status = array[i];
                if ( status.contains( ipOfNode ) )
                {
                    String decommissionStatus = array[i + 1];
                    if ( decommissionStatus.toLowerCase().contains( "normal".toLowerCase() ) )
                    {
                        decommissionState = NodeState.NORMAL;
                        break;
                    }
                    else if ( decommissionStatus.toLowerCase().contains( "progress".toLowerCase() ) )
                    {
                        decommissionState = NodeState.DECOMMISSION_IN_PROGRESS;
                        break;
                    }
                    else if ( decommissionStatus.toLowerCase().contains( "decommissioned".toLowerCase() ) )
                    {
                        decommissionState = NodeState.DECOMMISSIONED;
                        break;
                    }
                    else
                    {
                        decommissionState = NodeState.UNKNOWN;
                        break;
                    }
                }
            }
        }
        else
        {
            decommissionState = NodeState.UNKNOWN;
        }

        if ( decommissionState == NodeState.NORMAL && hadoopCluster.getBlockedAgents().contains( agent ) )
        {
            decommissionState = NodeState.DECOMMISSIONED;
        }

        return decommissionState;
    }


    protected void checkAllIfNoProcessRunning()
    {
        if ( getProcessCount() == 0 )
        {
            checkAllButton.click();
        }
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public synchronized int getProcessCount()
    {
        return processCount;
    }


    public Component getContent()
    {
        return contentRoot;
    }


    public Hadoop getHadoop()
    {
        return hadoop;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public ExecutorService getExecutorService()
    {
        return executorService;
    }


    public HadoopClusterConfig getHadoopCluster()
    {
        return hadoopCluster;
    }


    public GridLayout getContentRoot()
    {
        return contentRoot;
    }


    public ComboBox getClusterList()
    {
        return clusterList;
    }


    public Table getMasterNodesTable()
    {
        return masterNodesTable;
    }


    public Table getSlaveNodesTable()
    {
        return slaveNodesTable;
    }


    public Label getReplicationFactor()
    {
        return replicationFactor;
    }


    public Label getDomainName()
    {
        return domainName;
    }


    public Label getSlaveNodeCount()
    {
        return slaveNodeCount;
    }


    public Button getCheckAllButton()
    {
        return checkAllButton;
    }


    public String getDecommissionStatus()
    {
        return decommissionStatus;
    }


    public void setDecommissionStatus( String decommissionStatus )
    {
        this.decommissionStatus = decommissionStatus;
    }


    public void refreshClustersInfo()
    {
        managerListener.refreshClusterList();
    }
}
