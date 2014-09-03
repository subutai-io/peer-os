package org.safehaus.subutai.plugin.hive.ui.manager;


import com.google.common.collect.Sets;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.plugin.hive.ui.HiveUI;


public class Manager
{

    private final ComboBox clusterCombo;
    private final Table serverTable, clientsTable;
    private GridLayout contentRoot;
    private HiveConfig config;


    public Manager()
    {

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        serverTable = createTableTemplate( "Server node", true );
        clientsTable = createTableTemplate( "Nodes", false );
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
                config = ( HiveConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

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

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config == null )
                {
                    show( "Select cluster" );
                    return;
                }
                ConfirmationDialog alert = new ConfirmationDialog(
                    String.format( "Cluster '%s' will be destroyed. Continue?", config.getClusterName() ),
                    "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        destroyClusterHandler();
                    }
                } );

                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );

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

                HadoopClusterConfig hci = HiveUI.getHadoopManager().getCluster(
                        config.getHadoopClusterName());
                if ( hci == null )
                {
                    show( "Hadoop cluster info not found" );
                    return;
                }

                Set<Agent> set = new HashSet<>( hci.getAllNodes() );
                set.remove( config.getServer() );
                set.removeAll( config.getClients() );
                if ( set.isEmpty() )
                {
                    show( "All nodes in Hadoop cluster have Hive installed" );
                    return;
                }

                AddNodeWindow w = new AddNodeWindow( config, set );
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

        controlsContent.addComponent( clusterCombo );
        controlsContent.addComponent( refreshClustersBtn );
        controlsContent.addComponent( destroyClusterBtn );
        controlsContent.addComponent( addNodeBtn );

        VerticalLayout tablesLayout = new VerticalLayout();
        tablesLayout.setSizeFull();
        tablesLayout.setSpacing( true );

        tablesLayout.addComponent( serverTable );
        tablesLayout.addComponent( clientsTable );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( tablesLayout, 0, 1, 0, 9 );

    }


    private Table createTableTemplate( String caption, boolean server )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        if ( server )
        {
            table.addContainerProperty( "Check", Button.class, null );
            table.addContainerProperty( "Start", Button.class, null );
            table.addContainerProperty( "Stop", Button.class, null );
            table.addContainerProperty( "Restart", Button.class, null );
        }
        else
        {
            table.addContainerProperty( "Destroy", Button.class, null );
        }
        table.addContainerProperty( "Status", Embedded.class, null );
        table.setSizeFull();

        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                String lxcHostname = ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                Agent lxcAgent = HiveUI.getAgentManager().getAgentByHostname( lxcHostname );
                if ( lxcAgent != null )
                {
                    TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( lxcAgent ), HiveUI.getExecutor(),
                        HiveUI.getCommandRunner(), HiveUI.getAgentManager() );
                    contentRoot.getUI().addWindow( terminal.getWindow() );
                }
                else
                {
                    show( "Agent is not connected" );
                }
            }
        } );
        return table;
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( serverTable, true, config.getServer() );
            populateTable( clientsTable, false, config.getClients().toArray( new Agent[0] ) );
        }
        else
        {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        HiveConfig current = ( HiveConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        List<HiveConfig> clustersInfo = HiveUI.getManager().getClusters();
        if ( clustersInfo != null && clustersInfo.size() > 0 )
        {
            for ( HiveConfig ci : clustersInfo )
            {
                clusterCombo.addItem( ci );
                String cap = String.format( "%s [%s]", ci.getClusterName(),
                    ci.getHadoopClusterName() );
                clusterCombo.setItemCaption( ci, cap );
            }
            clusterCombo.setValue( current );
        }
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void destroyClusterHandler()
    {

        UUID trackID = HiveUI.getManager().uninstallCluster( config.getClusterName() );

        ProgressWindow window = new ProgressWindow( HiveUI.getExecutor(), HiveUI.getTracker(), trackID,
            HiveConfig.PRODUCT_KEY );
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


    private void populateTable( final Table table, boolean server, Agent... agents )
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
            final Button restartBtn = new Button( "Restart" );
            restartBtn.addStyleName( "default" );
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.addStyleName( "default" );
            final Embedded icon = new Embedded( "", new ThemeResource(
                "img/spinner.gif" ) );

            startBtn.setEnabled( false );
            stopBtn.setEnabled( false );
            restartBtn.setEnabled( false );
            icon.setVisible( false );

            final List items = new ArrayList();
            items.add( agent.getHostname() );
            if ( server )
            {
                items.add( checkBtn );
                items.add( startBtn );
                items.add( stopBtn );
                items.add( restartBtn );
            }
            else
            {
                items.add( destroyBtn );
                destroyBtn.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy node  %s?", agent.getHostname() ),
                            "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                UUID trackID = HiveUI.getManager().destroyNode(
                                    config.getClusterName(),
                                    agent.getHostname() );
                                ProgressWindow window = new ProgressWindow( HiveUI.getExecutor(), HiveUI.getTracker(),
                                    trackID, HiveConfig.PRODUCT_KEY );
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
            items.add( icon );

            table.addItem( items.toArray(), null );

            checkBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    icon.setVisible( true );
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    final UUID trackId = HiveUI.getManager().statusCheck(
                        config.getClusterName(), agent.getHostname() );
                    HiveUI.getExecutor().execute( new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            ProductOperationView po = null;
                            while ( po == null || po.getState() == ProductOperationState.RUNNING )
                            {
                                po = HiveUI.getTracker().getProductOperation(
                                    HiveConfig.PRODUCT_KEY, trackId );
                            }
                            boolean running = po.getState() == ProductOperationState.SUCCEEDED;
                            checkBtn.setEnabled( true );
                            startBtn.setEnabled( !running );
                            stopBtn.setEnabled( running );
                            restartBtn.setEnabled( running );
                            if ( destroyBtn != null )
                            {
                                destroyBtn.setEnabled( true );
                            }
                            icon.setVisible( false );
                        }
                    } );
                }
            } );

            startBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    final UUID trackID = HiveUI.getManager().startNode(
                        config.getClusterName(), agent.getHostname() );

                    ProgressWindow window = new ProgressWindow( HiveUI.getExecutor(), HiveUI.getTracker(), trackID,
                        HiveConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener()
                    {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent )
                        {
                            ProductOperationView po = HiveUI.getTracker()
                                .getProductOperation( HiveConfig.PRODUCT_KEY, trackID );
                            boolean started = po.getState() == ProductOperationState.SUCCEEDED;
                            checkBtn.setEnabled( true );
                            startBtn.setEnabled( !started );
                            stopBtn.setEnabled( started );
                            restartBtn.setEnabled( started );
                            if ( destroyBtn != null )
                            {
                                destroyBtn.setEnabled( true );
                            }
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
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    final UUID trackID = HiveUI.getManager().stopNode(
                        config.getClusterName(), agent.getHostname() );

                    ProgressWindow window = new ProgressWindow( HiveUI.getExecutor(), HiveUI.getTracker(), trackID,
                        HiveConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener()
                    {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent )
                        {
                            ProductOperationView po = HiveUI.getTracker()
                                .getProductOperation( HiveConfig.PRODUCT_KEY, trackID );
                            boolean stopped = po.getState() == ProductOperationState.SUCCEEDED;
                            checkBtn.setEnabled( true );
                            startBtn.setEnabled( stopped );
                            stopBtn.setEnabled( !stopped );
                            restartBtn.setEnabled( !stopped );
                            if ( destroyBtn != null )
                            {
                                destroyBtn.setEnabled( true );
                            }
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                }
            } );

            restartBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    for ( Object e : items )
                    {
                        if ( e instanceof Button )
                        {
                            ( ( Button ) e ).setEnabled( false );
                        }
                    }
                    final UUID trackID = HiveUI.getManager().restartNode(
                        config.getClusterName(), agent.getHostname() );

                    ProgressWindow window = new ProgressWindow( HiveUI.getExecutor(), HiveUI.getTracker(), trackID,
                        HiveConfig.PRODUCT_KEY );
                    window.getWindow().addCloseListener( new Window.CloseListener()
                    {
                        @Override
                        public void windowClose( Window.CloseEvent closeEvent )
                        {
                            ProductOperationView po = HiveUI.getTracker()
                                .getProductOperation( HiveConfig.PRODUCT_KEY, trackID );
                            boolean ok = po.getState() == ProductOperationState.SUCCEEDED;
                            checkBtn.setEnabled( true );
                            startBtn.setEnabled( !ok );
                            stopBtn.setEnabled( ok );
                            restartBtn.setEnabled( true );
                            if ( destroyBtn != null )
                            {
                                destroyBtn.setEnabled( true );
                            }
                        }
                    } );
                    contentRoot.getUI().addWindow( window.getWindow() );
                }
            } );

        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
