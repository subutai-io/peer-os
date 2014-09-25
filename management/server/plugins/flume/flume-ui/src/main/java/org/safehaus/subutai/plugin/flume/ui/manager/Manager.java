package org.safehaus.subutai.plugin.flume.ui.manager;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

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

    private final ExecutorService executorService;
    private final Flume flume;
    private final Tracker tracker;
    private final CommandRunner commandRunner;
    private final AgentManager agentManager;
    private GridLayout contentRoot;
    private ComboBox clusterCombo;
    private Table nodesTable;
    private FlumeConfig config;


    public Manager( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.flume = serviceLocator.getService( Flume.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        this.commandRunner = serviceLocator.getService( CommandRunner.class );
        this.agentManager = serviceLocator.getService( AgentManager.class );

        contentRoot = new GridLayout();
        contentRoot.setColumns( 1 );
        contentRoot.setRows( 10 );
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        getClusterNameLabel( controlsContent );
        getClusterCombo( controlsContent );
        getRefreshClusterButton( controlsContent );
        getDestroyClusterButton( controlsContent );
        getAddNodeButton( controlsContent );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    private void getAddNodeButton( HorizontalLayout controlsContent )
    {
        Button addNodeBtn = new Button( "Add Node" );
        addNodeBtn.addStyleName( "default" );
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    Set<Agent> nodes = new HashSet<>( config.getHadoopNodes() );
                    nodes.removeAll( config.getNodes() );
                    if ( !nodes.isEmpty() )
                    {
                        AddNodeWindow addNodeWindow =
                                new AddNodeWindow( flume, executorService, tracker, config, nodes );
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
                        show( "All nodes in corresponding Hadoop cluster have Flume installed" );
                    }
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( addNodeBtn );
    }


    private void getDestroyClusterButton( HorizontalLayout controlsContent )
    {
        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String m = "Are you sure to delete Flume nodes installed on Hadoop cluster '%s'?";
                    ConfirmationDialog alert =
                            new ConfirmationDialog( String.format( m, config.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = flume.uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
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
    }


    private void getRefreshClusterButton( HorizontalLayout controlsContent )
    {
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
    }


    private void getClusterCombo( HorizontalLayout controlsContent )
    {
        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( FlumeConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );
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
            final Button startBtn = new Button( "Start" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.addStyleName( "default" );
            stopBtn.setEnabled( true );
            startBtn.setEnabled( true );

            String ip = agent.getListIP() != null && !agent.getListIP().isEmpty() ? agent.getListIP().get( 0 ) : "";
            table.addItem( new Object[] {
                    agent.getHostname(), ip, startBtn, stopBtn, destroyBtn
            }, null );

            startBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    final UUID trackID = flume.startNode( config.getClusterName(), agent.getHostname() );
                    ProgressWindow window =
                            new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener()
                    {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent )
                        {
                            ProductOperationView po = tracker.getProductOperation( FlumeConfig.PRODUCT_KEY, trackID );
                            if ( po.getState() == ProductOperationState.SUCCEEDED )
                            {
                                stopBtn.setEnabled( true );
                            }
                            else
                            {
                                startBtn.setEnabled( true );
                            }
                            destroyBtn.setEnabled( true );
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                }
            } );

            stopBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    final UUID trackID = flume.stopNode( config.getClusterName(), agent.getHostname() );

                    ProgressWindow window =
                            new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener()
                    {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent )
                        {
                            ProductOperationView po = tracker.getProductOperation( FlumeConfig.PRODUCT_KEY, trackID );
                            if ( po.getState() == ProductOperationState.SUCCEEDED )
                            {
                                startBtn.setEnabled( true );
                            }
                            else
                            {
                                stopBtn.setEnabled( true );
                            }
                            destroyBtn.setEnabled( true );
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                }
            } );

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    String m = "Are you sure to remove Flume from node '%s'?";
                    ConfirmationDialog alert =
                            new ConfirmationDialog( String.format( m, agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = flume.destroyNode( config.getClusterName(), agent.getHostname() );
                            ProgressWindow window =
                                    new ProgressWindow( executorService, tracker, trackID, FlumeConfig.PRODUCT_KEY );
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
        List<FlumeConfig> clustersInfo = flume.getClusters();
        FlumeConfig clusterInfo = ( FlumeConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && !clustersInfo.isEmpty() )
        {
            for ( FlumeConfig ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                clusterCombo.setItemCaption( ci, ci.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( FlumeConfig ci : clustersInfo )
                {
                    if ( ci.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( ci );
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


    private void getClusterNameLabel( HorizontalLayout controlsContent )
    {
        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "IP address", String.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
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
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent agent = agentManager.getAgentByHostname( lxcHostname );
                    if ( agent != null )
                    {
                        Set<Agent> set = new HashSet<>( Arrays.asList( agent ) );
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
