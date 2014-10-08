/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mahout.ui.manager;


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
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.mahout.api.Mahout;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;
import org.safehaus.subutai.plugin.mahout.api.SetupType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String DESTROY_BUTTON_CAPTION = "Destroy";
    protected static final String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String BUTTON_STYLE_NAME = "default";
    final Button refreshClustersBtn, destroyClusterBtn, addNodeBtn;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final Hadoop hadoop;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final Mahout mahout;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private Table nodesTable;
    private MahoutClusterConfig config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.mahout = serviceLocator.getService( Mahout.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );
        contentRoot.setId("MahoutMngContentRoot");

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId("MahoutMngNodesTable");
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setId("MahoutMngClusterCombo");
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( MahoutClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId("MahoutMngRefresh");
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );


        destroyClusterBtn = new Button( DESTROY_CLUSTER_BUTTON_CAPTION );
        destroyClusterBtn.setId("MahoutMngDestroy");
        addClickListenerToDestroyClusterButton();
        controlsContent.addComponent( destroyClusterBtn );


        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId("MahoutMngAddNode");
        addClickListenerToAddNodeButton();
        controlsContent.addComponent( addNodeBtn );

        addStyleNameToButtons( refreshClustersBtn, destroyClusterBtn, addNodeBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    public void addClickListenerToDestroyClusterButton()
    {
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

                            UUID trackID = mahout.uninstallCluster( config.getClusterName() );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    MahoutClusterConfig.PRODUCT_KEY );
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
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( "Please, select cluster" );
                }
                else
                {
                    if ( config.getSetupType() == SetupType.OVER_HADOOP )
                    {
                        String hn = config.getHadoopClusterName();
                        if ( hn == null || hn.isEmpty() )
                        {
                            show( "Undefined Hadoop cluster name" );
                            return;
                        }
                        HadoopClusterConfig info = hadoop.getCluster( hn );
                        if ( info != null )
                        {
                            HashSet<Agent> nodes = new HashSet<>( info.getAllNodes() );
                            nodes.removeAll( config.getNodes() );
                            if ( !nodes.isEmpty() )
                            {
                                AddNodeWindow addNodeWindow =
                                        new AddNodeWindow( mahout, executorService, tracker, config, nodes );
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
                                show( "All nodes in corresponding Hadoop cluster have Presto installed" );
                            }
                        }
                        else
                        {
                            show( "Hadoop cluster info not found" );
                        }
                    }
                    else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                    {
                        ConfirmationDialog d = new ConfirmationDialog( "Add node to cluster", "OK", "Cancel" );
                        d.getOk().addClickListener( new Button.ClickListener()
                        {

                            @Override
                            public void buttonClick( Button.ClickEvent event )
                            {
                                UUID trackId = mahout.addNode( config.getClusterName(), null );
                                ProgressWindow w = new ProgressWindow( executorService, tracker, trackId,
                                        MahoutClusterConfig.PRODUCT_KEY );
                                contentRoot.getUI().addWindow( w.getWindow() );
                            }
                        } );
                        contentRoot.getUI().addWindow( d.getAlert() );
                    }
                }
            }
        } );
    }


    public void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( HOST_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( IP_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        addClickListenerToNodesTable( table );
        return table;
    }


    public void addClickListenerToNodesTable( final Table table )
    {
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


    private void populateTable( final Table table, Set<Agent> agents )
    {
        table.removeAllItems();
        for ( final Agent agent : agents )
        {
            final Button destroyBtn = new Button( DESTROY_BUTTON_CAPTION );
            destroyBtn.addStyleName( "default" );

            final HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.addStyleName( "default" );
            availableOperations.setSpacing( true );

            addGivenComponents( availableOperations, destroyBtn );

            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), availableOperations
            }, null );

            addClickListenerToDestroyButton( agent, destroyBtn );
        }
    }


    private void addClickListenerToDestroyButton( final Agent agent, Button... buttons )
    {
        getButton( DESTROY_BUTTON_CAPTION, buttons ).addClickListener( new Button.ClickListener()
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
                        UUID trackID = mahout.destroyNode( config.getClusterName(), agent.getHostname() );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                MahoutClusterConfig.PRODUCT_KEY );
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


    public void refreshClustersInfo()
    {
        List<MahoutClusterConfig> clustersInfo = mahout.getClusters();
        MahoutClusterConfig clusterInfo = ( MahoutClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( MahoutClusterConfig mongoClusterInfo : clustersInfo )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( MahoutClusterConfig mongoClusterInfo : clustersInfo )
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


    public void addGivenComponents( HorizontalLayout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
