package org.safehaus.subutai.plugin.hadoop.ui.manager;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.ui.BaseManager;
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
import com.vaadin.ui.Table;


public class Manager extends BaseManager {

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
    private Button checkAllButton;
    private HadoopClusterConfig hadoopCluster;
    private String decommissionStatus;
    private ManagerListener managerListener;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private final CommandRunner commandRunner;
    private final AgentManager agentManager;


    public Manager( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException {
        super();
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class);
        this.agentManager = serviceLocator.getService( AgentManager.class);

        managerListener = new ManagerListener( this );

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
            }
        } );

        controlsContent.addComponent( clusterList );

        checkAllButton = new Button( CHECK_ALL_BUTTON_CAPTION );
        checkAllButton.addStyleName( "default" );
        checkAllButton.addClickListener( managerListener.checkAllButtonListener( checkAllButton ) );

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

        Button destroyClusterButton = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterButton.addStyleName( "default" );
        destroyClusterButton.addClickListener( managerListener.destroyClusterButtonListener() );

        controlsContent.addComponent( destroyClusterButton );

        Button addNodeButton = new Button( ADD_NODE_BUTTON_CAPTION );
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


    public Table createTableTemplate( String caption ) {
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


    private void refreshUI() {
        if ( hadoopCluster != null ) {
//            refreshClustersInfo();
            populateTable( masterNodesTable, getMasterNodeList( hadoopCluster ) );
            populateTable( slaveNodesTable, hadoopCluster.getDataNodes() );
            replicationFactor.setValue( hadoopCluster.getReplicationFactor().toString() );
            domainName.setValue( hadoopCluster.getDomainName() );
            slaveNodeCount.setValue( hadoopCluster.getAllSlaveNodes().size() + "" );
        }
        else {
            masterNodesTable.removeAllItems();
            slaveNodesTable.removeAllItems();
            replicationFactor.setValue( "" );
            domainName.setValue( "" );
            slaveNodeCount.setValue( "" );
        }
    }


    private void startAllNodes( Table table ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
            if ( availableOperationsLayout != null ) {
                Button startButton = getStartButton( availableOperationsLayout );
                if ( startButton != null ) {
                    startButton.click();
                }
            }
        }
    }


    private void stopAllNodes( Table table ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
            if ( availableOperationsLayout != null ) {
                Button startButton = getStopButton( availableOperationsLayout );
                if ( startButton != null ) {
                    startButton.click();
                }
            }
        }
    }


    protected Button getExcludeIncludeButton( final HorizontalLayout availableOperationsLayout ) {
        if ( availableOperationsLayout == null ) {
            return null;
        }
        else {
            for ( Component component : availableOperationsLayout ) {
                if ( component.getCaption().equals( EXCLUDE_BUTTON_CAPTION )
                        || component.getCaption().equals( INCLUDE_BUTTON_CAPTION )
                        || component.getCaption().equals( EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION ) ) {
                    return ( Button ) component;
                }
            }
            // If not found
            return null;
        }
    }


    protected void show( String notification ) {
        Notification.show( notification );
    }


    @Override
    public void addRowComponents( Table table, final Agent agent ) {

        final HadoopClusterConfig cluster =
                hadoop.getCluster( hadoopCluster.getClusterName() );

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
        final Button destroyButton = new Button( DESTROY_BUTTON_CAPTION );
        final Button startStopButton = new Button( START_STOP_BUTTON_DEFAULT_CAPTION );
        final Button excludeIncludeNodeButton = new Button( EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION );
        final Button urlButton = new Button( URL_BUTTON_CAPTION );

        checkButton.addStyleName( "default" );
        startStopButton.addStyleName( "default" );
        excludeIncludeNodeButton.addStyleName( "default" );
        destroyButton.addStyleName( "default" );
        urlButton.addStyleName( "default" );
        // Buttons to be added to availableOperations


        // Labels to be added to statusGroup
        final Label statusDatanode = new Label( "" );
        final Label statusTaskTracker = new Label( "" );
        final Label statusDecommission = new Label( "" );

        statusDatanode.addStyleName( "default" );
        statusTaskTracker.addStyleName( "default" );
        statusDecommission.addStyleName( "default" );
        // Labels to be added to statusGroup


        if ( cluster.isMasterNode( agent ) ) {
            if ( cluster.isNameNode( agent ) ) {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( startStopButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusDatanode );
            }
            else if ( cluster.isJobTracker( agent ) ) {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( startStopButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusTaskTracker );
            }
            else if ( cluster.isSecondaryNameNode( agent ) ) {
                availableOperations.addComponent( checkButton );
                availableOperations.addComponent( urlButton );
                statusGroup.addComponent( statusDatanode );
            }
        }
        else {
            availableOperations.addComponent( checkButton );
            availableOperations.addComponent( excludeIncludeNodeButton );
            availableOperations.addComponent( destroyButton );
            statusGroup.addComponent( statusDatanode );
            statusGroup.addComponent( statusTaskTracker );
            statusGroup.addComponent( statusDecommission );
        }
        table.addItem( new Object[] {
                agent.getHostname(), agent.getListIP().toString(), getNodeRoles( cluster, agent ).toString(), statusGroup, availableOperations
        }, null );


        Item row = getAgentRow( table, agent );

        // Add listeners according to node type

        // If master node
        if ( cluster.isMasterNode( agent ) ) {

            // If Namenode
            if ( cluster.isNameNode( agent ) ) {
                urlButton.addClickListener( managerListener.nameNodeURLButtonListener( agent ) );
                checkButton.addClickListener( managerListener.nameNodeCheckButtonListener( row ) );
                startStopButton.addClickListener( managerListener.nameNodeStartStopButtonListener( row ) );

            }
            // If Jobtracker
            else if ( cluster.isJobTracker( agent ) ) {
                urlButton.addClickListener( jobTrackerURLButtonListener( agent) );
                checkButton.addClickListener( managerListener.jobTrackerCheckButtonListener( row ) );
                startStopButton.addClickListener( managerListener.jobTrackerStartStopButtonListener( row ) );
            }
            // If SecondaryNameNode
            else if ( cluster.isSecondaryNameNode( agent ) ) {
                urlButton.addClickListener( managerListener.secondaryNameNodeURLButtonListener( agent ) );
                checkButton.addClickListener( managerListener.secondaryNameNodeCheckButtonListener( row ) );

            }
        }
        // If slave node
        else {

            checkButton.addClickListener( managerListener.slaveNodeCheckButtonListener( row ) );
            excludeIncludeNodeButton.addClickListener( managerListener.slaveNodeExcludeIncludeButtonListener( row ) );
            destroyButton.addClickListener( managerListener.slaveNodeDestroyButtonListener( row ) );
        }
    }


    private List<NodeType> getNodeRoles( HadoopClusterConfig clusterConfig, final Agent agent )
    {
        List<NodeType> nodeRoles = new ArrayList<>();

        if ( clusterConfig.isNameNode( agent ) )
            nodeRoles.add( NodeType.NAMENODE );
        if ( clusterConfig.isSecondaryNameNode( agent ) )
            nodeRoles.add( NodeType.SECONDARY_NAMENODE );
        if ( clusterConfig.isJobTracker( agent ) )
            nodeRoles.add( NodeType.JOBTRACKER );
        if ( clusterConfig.isDataNode( agent ) )
            nodeRoles.add( NodeType.DATANODE );
        if ( clusterConfig.isTaskTracker( agent ) )
            nodeRoles.add( NodeType.TASKTRACKER );

        return nodeRoles;
    }


    private Button.ClickListener jobTrackerURLButtonListener( final Agent agent ) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent event ) {
                contentRoot.getUI().getPage().open( "http://" + agent.getListIP().get( 0 ) + ":50030",
                        "JobTracker", false );
            }
        } ;
    }


    protected Agent getAgentByRow( final Item row ) {
        if ( row == null ) {
            return null;
        }

        List<Agent> hadoopNodeList = hadoopCluster.getAllNodes();
        String lxcHostname= row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();

        for ( Agent agent : hadoopNodeList ) {
            if ( agent.getHostname().equals( lxcHostname ) ) {
                return agent;
            }
        }
        return null;
    }


    protected Label getStatusDatanodeLabel( final HorizontalLayout statusGroupLayout ) {
        if ( statusGroupLayout == null ) {
            return null;
        }
        return (Label) statusGroupLayout.getComponent( 0 );
    }


    protected Label getStatusTaskTrackerLabel( final HorizontalLayout statusGroupLayout ) {
        if ( statusGroupLayout == null ) {
            return null;
        }
        return (Label) statusGroupLayout.getComponent( 1 );
    }


    protected Label getStatusDecommissionLabel( final HorizontalLayout statusGroupLayout ) {
        if ( statusGroupLayout == null ) {
            return null;
        }
        return (Label) statusGroupLayout.getComponent( 2 );
    }


    protected NodeState getDecommissionStatus( final String operationLog, final Agent agent ) {
        NodeState decommissionState = NodeState.UNKNOWN;
        String ipOfNode = agent.getListIP().get( 0 );

        if ( operationLog != null && operationLog.contains( ipOfNode ) ) {
            String[] array = operationLog.split( "\n" );

            for ( int i = 0; i < array.length; i++ ) {
                String status = array[i];
                if ( status.contains( ipOfNode ) ) {
                    String decommissionStatus = array[i+1];
                    if ( decommissionStatus.toLowerCase().contains( "normal".toLowerCase() ) ) {
                        decommissionState = NodeState.NORMAL;
                        break;
                    }
                    else if ( decommissionStatus.toLowerCase().contains( "progress".toLowerCase() ) ) {
                        decommissionState = NodeState.DECOMMISSION_IN_PROGRESS;
                        break;
                    }
                    else if ( decommissionStatus.toLowerCase().contains( "decommissioned".toLowerCase() ) ) {
                        decommissionState= NodeState.DECOMMISSIONED;
                        break;
                    }
                    else {
                        decommissionState = NodeState.UNKNOWN;
                        break;
                    }
                }
            }
        }
        else {
            decommissionState = NodeState.UNKNOWN;
        }

        if ( decommissionState == NodeState.NORMAL && hadoopCluster.getBlockedAgents().contains( agent ) )
            decommissionState = NodeState.DECOMMISSIONED;

        return decommissionState;
    }


    protected void checkAllIfNoProcessRunning() {
        if ( getProcessCount() == 0 ) {
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


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public Hadoop getHadoop() {
        return hadoop;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }


    public HadoopClusterConfig getHadoopCluster() {
        return hadoopCluster;
    }


    public GridLayout getContentRoot() {
        return contentRoot;
    }

    public ComboBox getClusterList() {
        return clusterList;
    }

    public Table getMasterNodesTable() {
        return masterNodesTable;
    }

    public Table getSlaveNodesTable() {
        return slaveNodesTable;
    }

    public Label getReplicationFactor() {
        return replicationFactor;
    }

    public Label getDomainName() {
        return domainName;
    }

    public Label getSlaveNodeCount() {
        return slaveNodeCount;
    }

    public Button getCheckAllButton() {
        return checkAllButton;
    }

    public String getDecommissionStatus() {
        return decommissionStatus;
    }


    public void setDecommissionStatus( String decommissionStatus ) {
        this.decommissionStatus = decommissionStatus;
    }


    @Override
    public void refreshClustersInfo()
    {
        managerListener.refreshClusterList();
    }
}
