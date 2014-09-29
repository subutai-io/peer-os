package org.safehaus.subutai.plugin.shark.ui.manager;


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
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;


public class Manager
{

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private final ExecutorService executorService;
    private final Spark spark;
    private final Tracker tracker;
    private final Shark shark;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private SharkClusterConfig config;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.shark = serviceLocator.getService( Shark.class );
        this.spark = serviceLocator.getService( Spark.class );
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
                config = ( SharkClusterConfig ) event.getProperty().getValue();
                refreshUI();
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
                            UUID trackID = shark.uninstallCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                                                        SharkClusterConfig.PRODUCT_KEY );
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
                    show( "Please, select cluster" );
                    return;
                }
                SparkClusterConfig sparkInfo = spark.getCluster( config.getClusterName() );
                if ( sparkInfo != null )
                {
                    Set<Agent> nodes = new HashSet<>( sparkInfo.getAllNodes() );
                    nodes.removeAll( config.getNodes() );
                    if ( !nodes.isEmpty() )
                    {
                        AddNodeWindow win = new AddNodeWindow( shark, executorService, tracker, config, nodes );
                        contentRoot.getUI().addWindow( win );
                        win.addCloseListener( new Window.CloseListener()
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
                        show( "All nodes in corresponding Spark cluster have Shark installed" );
                    }
                }
                else
                {
                    show( "Spark cluster info not found" );
                }
            }


        } );

        controlsContent.addComponent( addNodeBtn );

        Button actualizeBtn = new Button( "Update Master" );
        actualizeBtn.addStyleName( "default" );
        actualizeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( "Please, select cluster" );
                    return;
                }
                ConfirmationDialog alert = new ConfirmationDialog( String.format(
                        "Do you want to Actualize master IP in %s cluster?", config.getClusterName() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID = shark.actualizeMasterIP( config.getClusterName() );
                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                                                    SharkClusterConfig.PRODUCT_KEY );
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
            }


        } );

        controlsContent.addComponent( actualizeBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String lxcHostname
                            = ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal
                                = new TerminalWindow( Sets.newHashSet( lxcAgent ), executorService, commandRunner,
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


    private void populateTable( final Table table, Set<Agent> agents )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.addStyleName( "default" );

            table.addItem( new Object[]
            {
                agent.getHostname() + String.format( " [%s]", agent.getListIP().get( 0 ) ), destroyBtn
            }, null );

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
                            UUID trackID = shark.destroyNode( config.getClusterName(), agent.getHostname() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                                                        SharkClusterConfig.PRODUCT_KEY );
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


    public void refreshClustersInfo()
    {
        List<SharkClusterConfig> clustersInfo = shark.getClusters();

        SharkClusterConfig clusterInfo = ( SharkClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( SharkClusterConfig mongoClusterInfo : clustersInfo )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( SharkClusterConfig mongoClusterInfo : clustersInfo )
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


    public Component getContent()
    {
        return contentRoot;
    }


}

