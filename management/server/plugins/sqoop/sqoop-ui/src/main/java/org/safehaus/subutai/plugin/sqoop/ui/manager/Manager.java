package org.safehaus.subutai.plugin.sqoop.ui.manager;


import java.util.Arrays;
import java.util.Collection;
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
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.ui.SqoopComponent;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class Manager
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String REFRESH_CLUSTERS_CAPTION = "Refresh Clusters";
    protected static final String DESTROY_ALL_INSTALLATIONS_BUTTON_CAPTION = "Destroy All Installations";
    protected static final String ADD_NODE_BUTTON_CAPTION = "Add Node";
    protected static final String HOST_COLUMN_CAPTION = "Host";
    protected static final String IP_COLUMN_CAPTION = "IP List";
    protected static final String BUTTON_STYLE_NAME = "default";
    final Button refreshClustersBtn, destroyClusterBtn, addNodeBtn;
    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ImportPanel importPanel;
    private final ExportPanel exportPanel;
    private final Sqoop sqoop;
    private final ExecutorService executorService;
    private final Tracker tracker;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final SqoopComponent sqoopComponent;
    private SqoopConfig config;
    private Hadoop hadoop;


    public Manager( ExecutorService executorService, ServiceLocator serviceLocator, SqoopComponent sqoopComponent )
            throws NamingException
    {

        this.executorService = executorService;
        this.sqoopComponent = sqoopComponent;
        this.sqoop = serviceLocator.getService( Sqoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        nodesTable.setId( "sqoopMngNodesTable" );
        contentRoot.setId( "sqoopMngContentRoot" );


        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select Sqoop installation:" );
        controlsContent.addComponent( clusterNameLabel );


        /**  Combo box  */
        clusterCombo = new ComboBox();
        clusterCombo.setId( "sqoopMngClusterCombo" );
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( SqoopConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );
        controlsContent.addComponent( clusterCombo );
        controlsContent.setComponentAlignment( clusterCombo, Alignment.MIDDLE_CENTER );


        refreshClustersBtn = new Button( REFRESH_CLUSTERS_CAPTION );
        refreshClustersBtn.setId( "sqoopMngRefresh" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );


        /** Destroy Cluster button */
        destroyClusterBtn = new Button( DESTROY_ALL_INSTALLATIONS_BUTTON_CAPTION );
        destroyClusterBtn.setId( "sqoopMngDestroy" );
        destroyClusterBtn.addStyleName( "default" );
        addClickListenerToDestorClusterButton();
        controlsContent.addComponent( destroyClusterBtn );


        /** Add Node button */
        addNodeBtn = new Button( ADD_NODE_BUTTON_CAPTION );
        addNodeBtn.setId( "sqoopMngAddNode" );
        addNodeBtn.addStyleName( "default" );
        addClickListenerToAddNodeButton();
        controlsContent.addComponent( addNodeBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );

        importPanel = new ImportPanel( sqoop, executorService, tracker );
        exportPanel = new ExportPanel( sqoop, executorService, tracker );
    }


    public void addClickListenerToDestorClusterButton()
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
                            UUID trackID = sqoop.uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( executorService, tracker, trackID, SqoopConfig.PRODUCT_KEY );
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
                if ( config != null )
                {
                    HadoopClusterConfig hadoopConfig = hadoop.getCluster( config.getHadoopClusterName() );
                    if ( hadoopConfig != null )
                    {
                        Set<Agent> nodes = new HashSet<>( hadoopConfig.getAllNodes() );
                        nodes.removeAll( config.getNodes() );
                        if ( !nodes.isEmpty() )
                        {
                            AddNodeWindow addNodeWindow =
                                    new AddNodeWindow( sqoop, tracker, executorService, config, nodes );
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
                            show( "All nodes in corresponding Hadoop cluster have Lucene installed" );
                        }
                    }
                    else
                    {
                        show( "Hadoop cluster info not found" );
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
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );

        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        table.setColumnCollapsingAllowed( true );

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
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        Set<Agent> set = new HashSet<>( Arrays.asList( lxcAgent ) );
                        TerminalWindow terminal =
                                new TerminalWindow( set, executorService, commandRunner, agentManager );
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


    public void addStyleNameToButtons( Button... buttons )
    {
        for ( Button b : buttons )
        {
            b.addStyleName( BUTTON_STYLE_NAME );
        }
    }


    public void addGivenComponents( Layout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private void populateTable( final Table table, Collection<Agent> agents )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Button importBtn = new Button( "Import" );
            importBtn.setId( agent.getListIP().get( 0 ) + "-sqoopImport" );
            final Button exportBtn = new Button( "Export" );
            exportBtn.setId( agent.getListIP().get( 0 ) + "-sqoopExport" );
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.setId( agent.getListIP().get( 0 ) + "-sqoopDestroy" );

            addStyleNameToButtons( importBtn, exportBtn, destroyBtn );

            HorizontalLayout availableOperations = new HorizontalLayout();
            availableOperations.setSpacing( true );
            availableOperations.addStyleName( "default" );

            addGivenComponents( availableOperations, importBtn, exportBtn, destroyBtn );

            table.addItem( new Object[] {
                    agent.getHostname(), agent.getListIP().get( 0 ), availableOperations
            }, null );


            importBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    importPanel.setAgent( agent );
                    importPanel.setType( null );
                    sqoopComponent.addTab( importPanel );
                }
            } );

            exportBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    exportPanel.setAgent( agent );
                    sqoopComponent.addTab( exportPanel );
                }
            } );

            addClickListenerToDestroyButton( agent, destroyBtn );
        }
    }


    public void addClickListenerToDestroyButton( final Agent agent, Button destroyBtn )
    {
        destroyBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", agent.getHostname() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackId = sqoop.destroyNode( config.getClusterName(), agent.getHostname() );
                        ProgressWindow window =
                                new ProgressWindow( executorService, tracker, trackId, SqoopConfig.PRODUCT_KEY );
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
        List<SqoopConfig> clusters = sqoop.getClusters();
        SqoopConfig clusterInfo = ( SqoopConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();

        if ( clusters == null || clusters.isEmpty() )
        {
            return;
        }

        for ( SqoopConfig esConfig : clusters )
        {
            clusterCombo.addItem( esConfig );
            clusterCombo.setItemCaption( esConfig, esConfig.getClusterName() );
        }

        if ( clusterInfo != null )
        {
            for ( SqoopConfig esConfig : clusters )
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
            clusterCombo.setValue( clusters.iterator().next() );
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
