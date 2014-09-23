package org.safehaus.subutai.plugin.elasticsearch.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
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


public class Manager
{

    protected final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected final static String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    protected final static String CHECK_BUTTON_CAPTION = "Check";
    protected final static String START_ALL_BUTTON_CAPTION = "Start All";
    protected final static String START_BUTTON_CAPTION = "Start";
    protected final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    protected final static String STOP_BUTTON_CAPTION = "Stop";
    protected final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected final static String DESTROY_BUTTON_CAPTION = "Destroy";
    protected final static String HOST_COLUMN_CAPTION = "Host";
    protected final static String IP_COLUMN_CAPTION = "IP List";
    protected final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    protected final static String STATUS_COLUMN_CAPTION = "Status";
    protected final static String ADD_NODE_CAPTION = "Add Node";
    private static final String message = "No cluster is installed !";
    private static final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
    private static final Pattern elasticsearchPattern = Pattern.compile( ".*(elasticsearch.+?g).*" );
    final Button refreshClustersBtn, startAllBtn, stopAllBtn, checkAllBtn, destroyClusterBtn;
    private final Table nodesTable;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final AgentManager agentManager;
    private final Elasticsearch elasticsearch;
    private final CommandRunner commandRunner;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private ElasticsearchClusterConfiguration config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.elasticsearch = serviceLocator.getService( Elasticsearch.class );
        this.executorService = executorService;
        this.tracker = serviceLocator.getService( Tracker.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );


        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Cluster nodes" );

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );
        controlsContent.setHeight( 100, Sizeable.Unit.PERCENTAGE );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
        controlsContent.setComponentAlignment( clusterNameLabel, Alignment.MIDDLE_CENTER );


        /**  Combo box  */
        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( ElasticsearchClusterConfiguration ) event.getProperty().getValue();
                refreshUI();
                checkAllNodes();
            }
        } );

        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );

        /**  Refresh clusters button */
        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
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
        controlsContent.setComponentAlignment( refreshClustersBtn, Alignment.MIDDLE_CENTER );


        /** Check all button */
        checkAllBtn = new Button( CHECK_ALL_BUTTON_CAPTION );
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
                    checkAllNodes();
                }
            }
        } );

        controlsContent.addComponent( checkAllBtn );
        controlsContent.setComponentAlignment( checkAllBtn, Alignment.MIDDLE_CENTER );


        /**  Start all button */
        startAllBtn = new Button( START_ALL_BUTTON_CAPTION );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
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

        controlsContent.addComponent( startAllBtn );
        controlsContent.setComponentAlignment( startAllBtn, Alignment.MIDDLE_CENTER );


        /**  Stop all button  */
        stopAllBtn = new Button( STOP_ALL_BUTTON_CAPTION );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
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

        controlsContent.addComponent( stopAllBtn );
        controlsContent.setComponentAlignment( stopAllBtn, Alignment.MIDDLE_CENTER );


        /**  Destroy cluster button  */
        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
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

                            UUID trackID = elasticsearch.uninstallCluster( config.getClusterName() );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    ElasticsearchClusterConfiguration.PRODUCT_KEY );

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
        progressIcon.setVisible( false );
        controlsContent.addComponent( progressIcon );
        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    /**
     * Parses output of 'service cassandra status' command
     */
    public static String parseServiceResult( String result )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher tracersMatcher = elasticsearchPattern.matcher( result );
        if ( tracersMatcher.find() )
        {
            parsedResult.append( tracersMatcher.group( 1 ) ).append( " " );
        }

        return parsedResult.toString();
    }


    public void startAllNodes()
    {
        for ( Agent agent : config.getNodes() )
        {
            progressIcon.setVisible( true );
            executorService.execute(
                    new StartTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                            new CompleteEvent()
                            {
                                @Override
                                public void onComplete( String result )
                                {
                                    synchronized ( progressIcon )
                                    {
                                        checkAllNodes();
                                    }
                                }
                            } ) );
        }
    }


    public void stopAllNodes()
    {
        for ( Agent agent : config.getNodes() )
        {
            progressIcon.setVisible( true );
            executorService.execute( new StopTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                    new CompleteEvent()
                    {
                        @Override
                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                checkAllNodes();
                            }
                        }
                    } ) );
        }
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
        table.setColumnCollapsingAllowed( true );

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
                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), executorService, commandRunner,
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
            populateTable( nodesTable, config.getNodes() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        List<ElasticsearchClusterConfiguration> config = elasticsearch.getClusters();
        ElasticsearchClusterConfiguration clusterInfo = ( ElasticsearchClusterConfiguration ) clusterCombo.getValue();
        clusterCombo.removeAllItems();

        if ( config == null || config.isEmpty() )
        {
            progressIcon.setVisible( false );
            return;
        }

        for ( ElasticsearchClusterConfiguration esConfig : config )
        {
            clusterCombo.addItem( esConfig );
            clusterCombo.setItemCaption( esConfig, esConfig.getClusterName() );
        }

        if ( clusterInfo != null )
        {
            for ( ElasticsearchClusterConfiguration esConfig : config )
            {
                if ( esConfig.getClusterName().equals( clusterInfo.getClusterName() ) )
                {
                    clusterCombo.setValue( esConfig );
                    return;
                }
            }
        }
        else
        {
            clusterCombo.setValue( config.iterator().next() );
        }
    }


    /**
     * Fill out the table in which all nodes in the cluster are listed.
     *
     * @param table table to be filled
     * @param agents nodes
     */
    private void populateTable( final Table table, Set<Agent> agents )
    {
        table.removeAllItems();
        for ( final Agent agent : agents )
        {
            final Label resultHolder = new Label();
            final Button checkButton = new Button( CHECK_BUTTON_CAPTION );
            checkButton.addStyleName( "default" );
            checkButton.setVisible( true );

            final Button startButton = new Button( START_BUTTON_CAPTION );
            startButton.addStyleName( "default" );
            startButton.setVisible( true );

            final Button stopButton = new Button( STOP_BUTTON_CAPTION );
            stopButton.addStyleName( "default" );
            stopButton.setVisible( true );

            startButton.setEnabled( false );
            stopButton.setEnabled( false );
            progressIcon.setVisible( false );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            availableOperations.addComponent( checkButton );
            availableOperations.addComponent( startButton );
            availableOperations.addComponent( stopButton );

            final Object rowId = table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), checkIfMaster( agent ), resultHolder,
                    availableOperations
            }, null );

            checkButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );
                    startButton.setEnabled( false );
                    stopButton.setEnabled( false );
                    checkButton.setEnabled( false );
                    executorService.execute(
                            new CheckTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                String status = parseServiceResult( result );
                                                resultHolder.setValue( status );
                                                if ( status.contains( "not" ) )
                                                {
                                                    startButton.setEnabled( true );
                                                    stopButton.setEnabled( false );
                                                }
                                                else
                                                {
                                                    startButton.setEnabled( false );
                                                    stopButton.setEnabled( true );
                                                }
                                                progressIcon.setVisible( false );
                                                checkButton.setEnabled( true );
                                            }
                                        }
                                    } ) );
                }
            } );

            startButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startButton.setEnabled( false );
                    stopButton.setEnabled( false );
                    checkButton.setEnabled( false );
                    executorService.execute(
                            new StartTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                startButton.setEnabled( true );
                                                stopButton.setEnabled( true );
                                                checkButton.setEnabled( true );
                                                checkButton.click();
                                            }
                                        }
                                    } ) );
                }
            } );

            stopButton.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    progressIcon.setVisible( true );
                    startButton.setEnabled( false );
                    stopButton.setEnabled( false );
                    checkButton.setEnabled( false );
                    executorService.execute(
                            new StopTask( elasticsearch, tracker, config.getClusterName(), agent.getHostname(),
                                    new CompleteEvent()
                                    {
                                        @Override
                                        public void onComplete( String result )
                                        {
                                            synchronized ( progressIcon )
                                            {
                                                startButton.setEnabled( true );
                                                stopButton.setEnabled( true );
                                                checkButton.setEnabled( true );
                                                checkButton.click();
                                            }
                                        }
                                    } ) );
                }
            } );
        }
    }


    /**
     * @param agent agent
     *
     * @return Yes if give agent is among seeds, otherwise returns No
     */
    public String checkIfMaster( Agent agent )
    {
        if ( config.getMasterNodes().contains( agent ) )
        {
            return "Master";
        }
        return "Data";
    }


    public Component getContent()
    {
        return contentRoot;
    }
}